package com.banking.gateway_service.web.kyc.dto.application;

public record AdminReviewHttpRequest(
        boolean approve,
        String rejectionReason
) {}
