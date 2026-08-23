package com.banking.cards_service.application.usecase.list_my_cards;

import com.banking.cards_service.application.usecase.list_my_cards.dto.ListMyCardsQuery;
import com.banking.cards_service.application.usecase.list_my_cards.dto.ListMyCardsResult;

public interface ListMyCardsUseCase {
    ListMyCardsResult list(ListMyCardsQuery query);
}
