package com.banking.cards_service.application.usecase.set_card_limits.dto;

import java.util.UUID;

public record SetCardLimitsCommand(
        UUID userId,
        UUID cardId,
        long dailyLimitCents,
        long monthlyLimitCents,
        long perTransactionLimitCents
) {}
