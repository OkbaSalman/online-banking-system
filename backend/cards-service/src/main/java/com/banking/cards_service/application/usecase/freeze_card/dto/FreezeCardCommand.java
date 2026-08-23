package com.banking.cards_service.application.usecase.freeze_card.dto;

import java.util.UUID;

public record FreezeCardCommand(
        UUID userId,
        UUID cardId
) {}
