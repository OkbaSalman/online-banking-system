package com.banking.cards_service.application.usecase.unfreeze_card.dto;

import java.util.UUID;

public record UnfreezeCardCommand(
        UUID userId,
        UUID cardId
) {}
