package com.banking.kyc_service.domain.model;

import java.time.Instant;
import java.util.UUID;

public record KycDocument(
        UUID id,
        UUID applicationId,
        UUID userId,
        DocumentType type,
        String objectKey,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String sha256,
        Instant uploadedAt
) {}