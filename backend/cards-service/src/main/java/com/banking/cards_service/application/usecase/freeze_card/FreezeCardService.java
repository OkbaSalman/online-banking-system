package com.banking.cards_service.application.usecase.freeze_card;

import com.banking.cards_service.application.port.CardRepositoryPort;
import com.banking.cards_service.application.port.KycClientPort;
import com.banking.cards_service.application.port.KycStatus;
import com.banking.cards_service.application.usecase.common.exception.NotFoundException;
import com.banking.cards_service.application.usecase.freeze_card.dto.FreezeCardCommand;
import com.banking.cards_service.application.usecase.freeze_card.dto.FreezeCardResult;
import com.banking.cards_service.domain.model.Card;
import com.banking.cards_service.domain.model.CardStatus;

public class FreezeCardService implements FreezeCardUseCase {

    private final CardRepositoryPort cards;
    private final KycClientPort kyc;

    public FreezeCardService(CardRepositoryPort cards, KycClientPort kyc) {
        this.cards = cards;
        this.kyc = kyc;
    }

    @Override
    public FreezeCardResult freeze(FreezeCardCommand command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        if (command.cardId() == null) {
            throw new IllegalArgumentException("card_id is required");
        }

        KycStatus status = kyc.getMyKycStatus();
        if (status != KycStatus.APPROVED) {
            throw new IllegalArgumentException("KYC not approved");
        }

        Card card = cards.findById(command.cardId())
                .orElseThrow(() -> new NotFoundException("Card not found"));

        if (!command.userId().equals(card.userId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        if (card.status() == CardStatus.FROZEN) {
            return new FreezeCardResult(card);
        }
        if (card.status() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Card not active");
        }

        Card frozen = card.withStatus(CardStatus.FROZEN);

        cards.save(frozen);
        return new FreezeCardResult(frozen);
    }
}
