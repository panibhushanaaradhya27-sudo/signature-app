package com.signatureapp.dto;

import com.signatureapp.model.AuditAction;
import java.time.Instant;

public record AuditResponse(
        Long id,
        AuditAction action,
        String actorEmail,
        String message,
        String ipAddress,
        Instant createdAt
) {
}
