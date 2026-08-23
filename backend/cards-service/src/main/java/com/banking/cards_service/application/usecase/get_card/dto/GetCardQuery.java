package com.banking.cards_service.application.usecase.get_card.dto;

import java.util.UUID;

public record GetCardQuery(
        UUID userId,
        UUID cardId
) {}
