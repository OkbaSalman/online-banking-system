package com.banking.cards_service.application.usecase.set_card_limits;

import com.banking.cards_service.application.port.CardRepositoryPort;
import com.banking.cards_service.application.usecase.common.exception.NotFoundException;
import com.banking.cards_service.application.usecase.set_card_limits.dto.SetCardLimitsCommand;
import com.banking.cards_service.application.usecase.set_card_limits.dto.SetCardLimitsResult;
import com.banking.cards_service.domain.model.Card;

public class SetCardLimitsService implements SetCardLimitsUseCase {

    private final CardRepositoryPort cards;

    public SetCardLimitsService(CardRepositoryPort cards) {
        this.cards = cards;
    }

    @Override
    public SetCardLimitsResult setLimits(SetCardLimitsCommand command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        if (command.cardId() == null) {
            throw new IllegalArgumentException("card_id is required");
        }
        if (command.dailyLimitCents() < 0 || command.monthlyLimitCents() < 0 || command.perTransactionLimitCents() < 0) {
            throw new IllegalArgumentException("limits must be >= 0");
        }

        Card card = cards.findById(command.cardId())
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!command.userId().equals(card.userId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        Card updated = card.withLimits(
                command.dailyLimitCents(),
                command.monthlyLimitCents(),
                command.perTransactionLimitCents()
        );
        cards.save(updated);
        return new SetCardLimitsResult(updated);
    }
}
