package com.signatureapp.repository;

import com.signatureapp.model.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
}
