package com.banking.kyc_service.application.usecase.kyc.adminReview.dto;

import java.util.UUID;

public record AdminReviewCommand(
        UUID reviewerUserId,
        UUID applicationId,
        boolean approve,
        String rejectionReason
) {}