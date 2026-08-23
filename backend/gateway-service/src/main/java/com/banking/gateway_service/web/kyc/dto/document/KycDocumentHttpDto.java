package com.banking.gateway_service.web.kyc.dto.document;

public record KycDocumentHttpDto(
        String id,
        String applicationId,
        String userId,
        String type,
        String objectKey,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String sha256,
        long uploadedAtEpochMs
) {}
