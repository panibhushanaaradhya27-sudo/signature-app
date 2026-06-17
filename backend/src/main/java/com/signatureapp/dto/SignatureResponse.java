package com.signatureapp.dto;

import com.signatureapp.model.SignatureStatus;
import java.time.Instant;

public record SignatureResponse(
        Long id,
        Long documentId,
        String signerEmail,
        String signerName,
        float x,
        float y,
        int pageNumber,
        String publicToken,
        String publicUrl,
        SignatureStatus status,
        String rejectionReason,
        Instant createdAt,
        Instant completedAt
) {
}
