package com.signatureapp.service;

import com.signatureapp.dto.PublicSignRequest;
import com.signatureapp.dto.RejectRequest;
import com.signatureapp.dto.SignatureRequestDto;
import com.signatureapp.dto.SignatureResponse;
import com.signatureapp.model.AuditAction;
import com.signatureapp.model.DocumentStatus;
import com.signatureapp.model.SignatureRequest;
import com.signatureapp.model.SignatureStatus;
import com.signatureapp.model.User;
import com.signatureapp.repository.DocumentRepository;
import com.signatureapp.repository.SignatureRequestRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignatureService {
    private final SignatureRequestRepository signatureRequestRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final AuditService auditService;
    private final EmailService emailService;
    private final PdfSigningService pdfSigningService;
    private final String publicUrl;

    public SignatureService(
            SignatureRequestRepository signatureRequestRepository,
            DocumentRepository documentRepository,
            DocumentService documentService,
            AuditService auditService,
            EmailService emailService,
            PdfSigningService pdfSigningService,
            @Value("${app.public-url}") String publicUrl
    ) {
        this.signatureRequestRepository = signatureRequestRepository;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.auditService = auditService;
        this.emailService = emailService;
        this.pdfSigningService = pdfSigningService;
        this.publicUrl = publicUrl;
    }

    @Transactional
    public SignatureResponse create(SignatureRequestDto request, User owner, String ipAddress) {
        var document = documentService.ownedDocument(request.documentId(), owner);
        var signature = new SignatureRequest();
        signature.setDocument(document);
        signature.setSignerEmail(request.signerEmail().toLowerCase());
        signature.setSignerName(request.signerName());
        signature.setX(request.x());
        signature.setY(request.y());
        signature.setPageNumber(Math.max(1, request.pageNumber()));
        signature.setPublicToken(UUID.randomUUID().toString());
        signatureRequestRepository.save(signature);

        document.setStatus(DocumentStatus.PENDING);
        documentRepository.save(document);
        auditService.record(document, owner, AuditAction.CREATE_SIGNATURE_REQUEST,
                "Signature request created for " + signature.getSignerEmail(), ipAddress);
        emailService.sendSignatureLink(
                signature.getSignerEmail(),
                signature.getSignerName(),
                document.getOriginalName(),
                publicLink(signature)
        );
        auditService.record(document, owner, AuditAction.SEND_PUBLIC_LINK,
                "Public signing link generated", ipAddress);
        return toResponse(signature);
    }

    public List<SignatureResponse> list(Long documentId, User owner) {
        documentService.ownedDocument(documentId, owner);
        return signatureRequestRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SignatureResponse publicSign(String token, PublicSignRequest request, String ipAddress) throws IOException {
        var signature = signatureRequestRepository.findByPublicToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid signing link"));
        if (signature.getStatus() != SignatureStatus.PENDING) {
            throw new IllegalStateException("This request is already completed");
        }
        if (request.signerName() != null && !request.signerName().isBlank()) {
            signature.setSignerName(request.signerName());
        }
        signature.setStatus(SignatureStatus.SIGNED);
        signature.setCompletedAt(Instant.now());
        signatureRequestRepository.save(signature);

        var document = signature.getDocument();
        auditService.record(document, null, AuditAction.SIGN_DOCUMENT,
                signature.getSignerEmail() + " signed the document", ipAddress);
        finalizeIfReady(document, ipAddress);
        return toResponse(signature);
    }

    @Transactional
    public SignatureResponse reject(String token, RejectRequest request, String ipAddress) {
        var signature = signatureRequestRepository.findByPublicToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid signing link"));
        signature.setStatus(SignatureStatus.REJECTED);
        signature.setRejectionReason(request.reason());
        signature.setCompletedAt(Instant.now());
        signatureRequestRepository.save(signature);
        documentService.refreshStatus(signature.getDocument());
        auditService.record(signature.getDocument(), null, AuditAction.REJECT_DOCUMENT,
                signature.getSignerEmail() + " rejected the document", ipAddress);
        return toResponse(signature);
    }

    @Transactional
    public void finalizeOwned(Long documentId, User owner, String ipAddress) throws IOException {
        var document = documentService.ownedDocument(documentId, owner);
        finalizeIfReady(document, ipAddress);
        auditService.record(document, owner, AuditAction.FINALIZE_DOCUMENT, "Finalize requested", ipAddress);
    }

    public SignatureResponse publicInfo(String token) {
        var signature = signatureRequestRepository.findByPublicToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid signing link"));
        return toResponse(signature);
    }

    private void finalizeIfReady(com.signatureapp.model.Document document, String ipAddress) throws IOException {
        var signatures = signatureRequestRepository.findByDocumentIdOrderByCreatedAtAsc(document.getId());
        if (!signatures.isEmpty() && signatures.stream().allMatch(sig -> sig.getStatus() == SignatureStatus.SIGNED)) {
            String path = pdfSigningService.generateSignedPdf(document, signatures);
            document.setSignedStoragePath(path);
            documentService.refreshStatus(document);
            auditService.record(document, null, AuditAction.FINALIZE_DOCUMENT,
                    "Signed PDF generated", ipAddress);
        } else {
            documentService.refreshStatus(document);
        }
    }

    private SignatureResponse toResponse(SignatureRequest signature) {
        return new SignatureResponse(
                signature.getId(),
                signature.getDocument().getId(),
                signature.getSignerEmail(),
                signature.getSignerName(),
                signature.getX(),
                signature.getY(),
                signature.getPageNumber(),
                signature.getPublicToken(),
                publicLink(signature),
                signature.getStatus(),
                signature.getRejectionReason(),
                signature.getCreatedAt(),
                signature.getCompletedAt()
        );
    }

    private String publicLink(SignatureRequest signature) {
        return publicUrl + "/sign/" + signature.getPublicToken();
    }
}
