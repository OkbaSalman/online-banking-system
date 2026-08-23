package com.banking.cards_service.application.usecase.list_my_cards.dto;

import com.banking.cards_service.domain.model.Card;

import java.util.List;

public record ListMyCardsResult(
        List<Card> cards
) {}
