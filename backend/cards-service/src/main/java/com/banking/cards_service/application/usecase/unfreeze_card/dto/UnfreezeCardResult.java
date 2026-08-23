package com.banking.cards_service.application.usecase.unfreeze_card.dto;

import com.banking.cards_service.domain.model.Card;

public record UnfreezeCardResult(
        Card card
) {}
