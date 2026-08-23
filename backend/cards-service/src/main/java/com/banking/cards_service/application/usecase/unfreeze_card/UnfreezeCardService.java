package com.banking.cards_service.application.usecase.unfreeze_card;

import com.banking.cards_service.application.port.CardRepositoryPort;
import com.banking.cards_service.application.port.KycClientPort;
import com.banking.cards_service.application.port.KycStatus;
import com.banking.cards_service.application.usecase.common.exception.NotFoundException;
import com.banking.cards_service.application.usecase.unfreeze_card.dto.UnfreezeCardCommand;
import com.banking.cards_service.application.usecase.unfreeze_card.dto.UnfreezeCardResult;
import com.banking.cards_service.domain.model.Card;
import com.banking.cards_service.domain.model.CardStatus;

public class UnfreezeCardService implements UnfreezeCardUseCase {

    private final CardRepositoryPort cards;
    private final KycClientPort kyc;

    public UnfreezeCardService(CardRepositoryPort cards, KycClientPort kyc) {
        this.cards = cards;
        this.kyc = kyc;
    }

    @Override
    public UnfreezeCardResult unfreeze(UnfreezeCardCommand command) {
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

        if (card.status() == CardStatus.ACTIVE) {
            return new UnfreezeCardResult(card);
        }
        if (card.status() != CardStatus.FROZEN) {
            throw new IllegalArgumentException("Card not frozen");
        }

        Card active = card.withStatus(CardStatus.ACTIVE);

        cards.save(active);
        return new UnfreezeCardResult(active);
    }
}
