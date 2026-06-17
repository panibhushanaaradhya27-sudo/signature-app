package com.signatureapp.service;

import com.signatureapp.dto.AuditResponse;
import com.signatureapp.model.AuditAction;
import com.signatureapp.model.AuditLog;
import com.signatureapp.model.Document;
import com.signatureapp.model.User;
import com.signatureapp.repository.AuditLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(Document document, User actor, AuditAction action, String message, String ipAddress) {
        var log = new AuditLog();
        log.setDocument(document);
        log.setActor(actor);
        log.setAction(action);
        log.setMessage(message);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    public List<AuditResponse> forDocument(Long documentId) {
        return auditLogRepository.findByDocumentIdOrderByCreatedAtDesc(documentId)
                .stream()
                .map(log -> new AuditResponse(
                        log.getId(),
                        log.getAction(),
                        log.getActor() == null ? "public-signer" : log.getActor().getEmail(),
                        log.getMessage(),
                        log.getIpAddress(),
                        log.getCreatedAt()
                ))
                .toList();
    }
}
