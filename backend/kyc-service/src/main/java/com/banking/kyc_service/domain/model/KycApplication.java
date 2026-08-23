package com.banking.kyc_service.domain.model;

import java.time.Instant;
import java.util.UUID;

public record KycApplication(
        UUID id,
        UUID userId,
        KycStatus status,
        String fullName,
        String nationalId,
        String address,
        UUID reviewerUserId,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {}