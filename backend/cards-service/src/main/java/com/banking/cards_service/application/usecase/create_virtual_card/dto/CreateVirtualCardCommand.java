package com.banking.cards_service.application.usecase.create_virtual_card.dto;

import java.util.UUID;

public record CreateVirtualCardCommand(
        UUID userId,
        UUID fundingAccountId,
        String idempotencyKey,
        String nickname,
        long dailyLimitCents,
        long monthlyLimitCents,
        long perTransactionLimitCents
) {}
