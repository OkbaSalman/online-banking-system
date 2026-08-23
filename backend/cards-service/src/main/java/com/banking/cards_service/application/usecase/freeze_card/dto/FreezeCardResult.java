package com.banking.cards_service.application.usecase.freeze_card.dto;

import com.banking.cards_service.domain.model.Card;

public record FreezeCardResult(
        Card card
) {}
