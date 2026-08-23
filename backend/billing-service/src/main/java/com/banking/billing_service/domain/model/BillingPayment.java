package com.banking.billing_service.domain.model;

import java.util.UUID;

public record BillingPayment(
        UUID id,
        UUID userId,
        UUID fromAccountId,
        UUID merchantAccountId,
        long amountCents,
        long createdAtEpochMs,
        BillingPaymentStatus status,
        String idempotencyKey,
        String description,
        UUID transferId,
        String failureMessage,
        UUID subscriptionId
) {}
