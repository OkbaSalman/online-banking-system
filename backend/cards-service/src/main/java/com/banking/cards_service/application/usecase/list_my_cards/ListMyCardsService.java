package com.banking.cards_service.application.usecase.list_my_cards;

import com.banking.cards_service.application.port.CardQueryPort;
import com.banking.cards_service.application.usecase.list_my_cards.dto.ListMyCardsQuery;
import com.banking.cards_service.application.usecase.list_my_cards.dto.ListMyCardsResult;

public class ListMyCardsService implements ListMyCardsUseCase {

    private final CardQueryPort cards;

    public ListMyCardsService(CardQueryPort cards) {
        this.cards = cards;
    }

    @Override
    public ListMyCardsResult list(ListMyCardsQuery query) {
        if (query.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }

        return new ListMyCardsResult(cards.listByUserId(query.userId(), query.limit(), query.offset()));
    }
}
