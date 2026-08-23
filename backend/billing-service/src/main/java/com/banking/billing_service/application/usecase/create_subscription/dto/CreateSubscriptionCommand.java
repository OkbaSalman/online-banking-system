package com.banking.billing_service.application.usecase.create_subscription.dto;

import com.banking.billing_service.domain.model.IntervalUnit;

import java.util.UUID;

public record CreateSubscriptionCommand(
        UUID userId,
        UUID fromAccountId,
        UUID merchantAccountId,
        long amountCents,
        IntervalUnit intervalUnit,
        int intervalCount,
        long startAtEpochMs,
        String idempotencyKey,
        String description
) {}
