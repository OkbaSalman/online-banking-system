package com.banking.cards_service.application.usecase.create_virtual_card.dto;

import com.banking.cards_service.domain.model.Card;

public record CreateVirtualCardResult(
        Card card
) {}
