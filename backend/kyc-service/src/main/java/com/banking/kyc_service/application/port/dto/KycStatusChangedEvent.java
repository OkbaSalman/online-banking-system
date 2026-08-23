package com.banking.kyc_service.application.port.dto;

import java.util.UUID;

public record KycStatusChangedEvent(
        UUID userId,
        UUID applicationId,
        String status,
        String reviewerUserId,
        String rejectionReason,
        long timestampEpochMs
) {}