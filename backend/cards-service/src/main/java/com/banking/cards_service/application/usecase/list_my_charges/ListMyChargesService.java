package com.banking.cards_service.application.usecase.list_my_charges;

import com.banking.cards_service.application.port.CardRepositoryPort;
import com.banking.cards_service.application.port.CardChargeQueryPort;
import com.banking.cards_service.application.usecase.common.exception.NotFoundException;
import com.banking.cards_service.application.usecase.list_my_charges.dto.ListMyChargesQuery;
import com.banking.cards_service.application.usecase.list_my_charges.dto.ListMyChargesResult;

public class ListMyChargesService implements ListMyChargesUseCase {

    private final CardRepositoryPort cards;
    private final CardChargeQueryPort charges;

    public ListMyChargesService(CardRepositoryPort cards, CardChargeQueryPort charges) {
        this.cards = cards;
        this.charges = charges;
    }

    @Override
    public ListMyChargesResult list(ListMyChargesQuery query) {
        if (query.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }

        if (query.cardId() == null) {
            return new ListMyChargesResult(charges.listByUserId(query.userId(), query.limit(), query.offset()));
        }

        var card = cards.findById(query.cardId()).orElseThrow(() -> new NotFoundException("Card not found"));
        if (!query.userId().equals(card.userId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        return new ListMyChargesResult(charges.listByCardId(query.cardId(), query.limit(), query.offset()));
    }
}
