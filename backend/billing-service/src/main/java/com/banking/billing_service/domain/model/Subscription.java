package com.banking.billing_service.domain.model;

import java.util.UUID;

public record Subscription(
        UUID id,
        UUID userId,
        UUID fromAccountId,
        UUID merchantAccountId,
        long amountCents,
        IntervalUnit intervalUnit,
        int intervalCount,
        long nextChargeAtEpochMs,
        SubscriptionStatus status,
        long createdAtEpochMs,
        String idempotencyKey,
        String description,
        Long lastAttemptAtEpochMs,
        int consecutiveFailures,
        long dueAnchorEpochMs
) {
    public Subscription {
        if (consecutiveFailures < 0) {
            consecutiveFailures = 0;
        }
        if (dueAnchorEpochMs <= 0) {
            dueAnchorEpochMs = nextChargeAtEpochMs;
        }
    }
}
