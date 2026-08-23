package com.banking.gateway_service.web.kyc.dto.document;

public record CreateDocumentSlotHttpRequest(
        String type,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String sha256
) {}
