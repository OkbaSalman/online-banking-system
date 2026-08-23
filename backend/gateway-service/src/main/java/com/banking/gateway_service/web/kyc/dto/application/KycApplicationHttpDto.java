package com.banking.gateway_service.web.kyc.dto.application;

public record KycApplicationHttpDto(
        String id,
        String userId,
        String status,
        String fullName,
        String nationalId,
        String address,
        String reviewerUserId,
        String rejectionReason,
        long createdAtEpochMs,
        long updatedAtEpochMs
) {}
