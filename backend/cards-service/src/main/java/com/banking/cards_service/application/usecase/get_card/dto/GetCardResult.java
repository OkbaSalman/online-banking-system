package com.banking.cards_service.application.usecase.get_card.dto;

import com.banking.cards_service.domain.model.Card;

public record GetCardResult(
        Card card
) {}
