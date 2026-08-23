package com.banking.cards_service.domain.model;

import java.util.UUID;

public record CardCharge(
        UUID id,
        UUID userId,
        UUID cardId,
        UUID merchantAccountId,
        long amountCents,
        long createdAtEpochMs,
        CardChargeStatus status,
        String idempotencyKey,
        String description,
        UUID transferId,
        String failureMessage,
        long feeCents
) {
    public CardCharge {
        if (feeCents < 0) feeCents = 0;
    }
}
