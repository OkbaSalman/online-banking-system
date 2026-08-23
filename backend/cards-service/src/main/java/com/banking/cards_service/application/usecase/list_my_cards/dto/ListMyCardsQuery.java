package com.banking.cards_service.application.usecase.list_my_cards.dto;

import java.util.UUID;

public record ListMyCardsQuery(
        UUID userId,
        int limit,
        int offset
) {}
