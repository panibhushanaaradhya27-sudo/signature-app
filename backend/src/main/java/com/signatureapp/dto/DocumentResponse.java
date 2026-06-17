package com.signatureapp.dto;

import com.signatureapp.model.DocumentStatus;
import java.time.Instant;

public record DocumentResponse(
        Long id,
        String originalName,
        DocumentStatus status,
        long sizeBytes,
        String previewUrl,
        String signedPreviewUrl,
        Instant createdAt,
        Instant finalizedAt
) {
}
