package com.signatureapp.dto;

public record SignatureRequestDto(
        Long documentId,
        String signerEmail,
        String signerName,
        float x,
        float y,
        int pageNumber
) {
}
