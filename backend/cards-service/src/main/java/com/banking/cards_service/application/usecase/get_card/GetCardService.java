package com.banking.cards_service.application.usecase.get_card;

import com.banking.cards_service.application.port.CardRepositoryPort;
import com.banking.cards_service.application.usecase.common.exception.NotFoundException;
import com.banking.cards_service.application.usecase.get_card.dto.GetCardQuery;
import com.banking.cards_service.application.usecase.get_card.dto.GetCardResult;

public class GetCardService implements GetCardUseCase {

    private final CardRepositoryPort cards;

    public GetCardService(CardRepositoryPort cards) {
        this.cards = cards;
    }

    @Override
    public GetCardResult get(GetCardQuery query) {
        if (query.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        if (query.cardId() == null) {
            throw new IllegalArgumentException("card_id is required");
        }

        var card = cards.findById(query.cardId())
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!query.userId().equals(card.userId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        return new GetCardResult(card);
    }
}
