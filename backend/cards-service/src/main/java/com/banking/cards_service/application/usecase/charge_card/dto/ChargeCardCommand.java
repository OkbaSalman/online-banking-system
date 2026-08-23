package com.banking.cards_service.application.usecase.charge_card.dto;

import java.util.UUID;

public record ChargeCardCommand(
        UUID userId,
        UUID cardId,
        UUID merchantAccountId,
        long amountCents,
        String idempotencyKey,
        String description
) {}
