package com.banking.cards_service.domain.model;

import java.util.UUID;

public record Card(
        UUID id,
        UUID userId,
        UUID fundingAccountId,
        String last4,
        CardStatus status,
        long createdAtEpochMs,
        String idempotencyKey,
        String nickname,
        long dailyLimitCents,
        long monthlyLimitCents,
        long perTransactionLimitCents
) {
    public Card {
        if (dailyLimitCents < 0) dailyLimitCents = 0;
        if (monthlyLimitCents < 0) monthlyLimitCents = 0;
        if (perTransactionLimitCents < 0) perTransactionLimitCents = 0;
    }

    public Card withStatus(CardStatus nextStatus) {
        return new Card(
                id, userId, fundingAccountId, last4, nextStatus, createdAtEpochMs, idempotencyKey, nickname,
                dailyLimitCents, monthlyLimitCents, perTransactionLimitCents
        );
    }

    public Card withLimits(long daily, long monthly, long perTransaction) {
        return new Card(
                id, userId, fundingAccountId, last4, status, createdAtEpochMs, idempotencyKey, nickname,
                daily, monthly, perTransaction
        );
    }
}
