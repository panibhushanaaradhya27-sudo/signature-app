package com.signatureapp.service;

import com.signatureapp.dto.DocumentResponse;
import com.signatureapp.model.AuditAction;
import com.signatureapp.model.Document;
import com.signatureapp.model.DocumentStatus;
import com.signatureapp.model.SignatureStatus;
import com.signatureapp.model.User;
import com.signatureapp.repository.DocumentRepository;
import com.signatureapp.repository.SignatureRequestRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final SignatureRequestRepository signatureRequestRepository;
    private final AuditService auditService;
    private final Path uploadDir;

    public DocumentService(
            DocumentRepository documentRepository,
            SignatureRequestRepository signatureRequestRepository,
            AuditService auditService,
            @Value("${app.storage.upload-dir}") String uploadDir
    ) {
        this.documentRepository = documentRepository;
        this.signatureRequestRepository = signatureRequestRepository;
        this.auditService = auditService;
        this.uploadDir = Path.of(uploadDir);
    }

    @Transactional
    public DocumentResponse upload(MultipartFile file, User owner, String ipAddress) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a PDF file");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        Files.createDirectories(uploadDir);
        String storedName = UUID.randomUUID() + ".pdf";
        Path target = uploadDir.resolve(storedName).normalize();
        file.transferTo(target);

        var document = new Document();
        document.setOriginalName(file.getOriginalFilename() == null ? "document.pdf" : file.getOriginalFilename());
        document.setStoredName(storedName);
        document.setContentType(file.getContentType());
        document.setSizeBytes(file.getSize());
        document.setStoragePath(target.toString());
        document.setOwner(owner);
        documentRepository.save(document);
        auditService.record(document, owner, AuditAction.UPLOAD_DOCUMENT, "Document uploaded", ipAddress);
        return toResponse(document);
    }

    public List<DocumentResponse> list(User owner) {
        return documentRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DocumentResponse getOwned(Long id, User owner, String ipAddress) {
        var document = ownedDocument(id, owner);
        auditService.record(document, owner, AuditAction.VIEW_DOCUMENT, "Document viewed", ipAddress);
        return toResponse(document);
    }

    public Resource resource(Long id, User owner, boolean signed) {
        var document = ownedDocument(id, owner);
        return resourceFor(document, signed);
    }

    public Resource publicResource(String token) {
        var signature = signatureRequestRepository.findByPublicToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid signing link"));
        return resourceFor(signature.getDocument(), false);
    }

    public Document ownedDocument(Long id, User owner) {
        var document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (!document.getOwner().getId().equals(owner.getId())) {
            throw new SecurityException("You do not have access to this document");
        }
        return document;
    }

    public void refreshStatus(Document document) {
        var signatures = signatureRequestRepository.findByDocumentIdOrderByCreatedAtAsc(document.getId());
        if (signatures.isEmpty()) {
            document.setStatus(DocumentStatus.DRAFT);
        } else if (signatures.stream().anyMatch(sig -> sig.getStatus() == SignatureStatus.REJECTED)) {
            document.setStatus(DocumentStatus.REJECTED);
        } else if (signatures.stream().allMatch(sig -> sig.getStatus() == SignatureStatus.SIGNED)) {
            document.setStatus(DocumentStatus.SIGNED);
            document.setFinalizedAt(Instant.now());
        } else {
            document.setStatus(DocumentStatus.PENDING);
        }
        documentRepository.save(document);
    }

    public DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getOriginalName(),
                document.getStatus(),
                document.getSizeBytes(),
                "/api/docs/" + document.getId() + "/file",
                document.getSignedStoragePath() == null ? null : "/api/docs/" + document.getId() + "/signed-file",
                document.getCreatedAt(),
                document.getFinalizedAt()
        );
    }

    private Resource resourceFor(Document document, boolean signed) {
        String path = signed && document.getSignedStoragePath() != null
                ? document.getSignedStoragePath()
                : document.getStoragePath();
        return new FileSystemResource(Path.of(path));
    }
}
