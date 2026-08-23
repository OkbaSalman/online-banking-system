package com.banking.cards_service.application.usecase.create_virtual_card;

import com.banking.cards_service.application.port.CardRepositoryPort;
import com.banking.cards_service.application.port.KycClientPort;
import com.banking.cards_service.application.port.KycStatus;
import com.banking.cards_service.application.usecase.create_virtual_card.dto.CreateVirtualCardCommand;
import com.banking.cards_service.application.usecase.create_virtual_card.dto.CreateVirtualCardResult;
import com.banking.cards_service.domain.model.Card;
import com.banking.cards_service.domain.model.CardStatus;

import java.util.Optional;
import java.util.UUID;

public class CreateVirtualCardService implements CreateVirtualCardUseCase {

    private final CardRepositoryPort cards;
    private final KycClientPort kyc;

    public CreateVirtualCardService(CardRepositoryPort cards, KycClientPort kyc) {
        this.cards = cards;
        this.kyc = kyc;
    }

    @Override
    public CreateVirtualCardResult create(CreateVirtualCardCommand command) {
        validate(command);

        Optional<Card> existing = cards.findByUserIdAndIdempotencyKey(command.userId(), command.idempotencyKey());
        if (existing.isPresent()) {
            return new CreateVirtualCardResult(existing.get());
        }

        KycStatus status = kyc.getMyKycStatus();
        if (status != KycStatus.APPROVED) {
            throw new IllegalArgumentException("KYC not approved");
        }

        long now = System.currentTimeMillis();
        Card card = new Card(
                UUID.randomUUID(),
                command.userId(),
                command.fundingAccountId(),
                generateLast4(),
                CardStatus.ACTIVE,
                now,
                command.idempotencyKey(),
                command.nickname(),
                command.dailyLimitCents(),
                command.monthlyLimitCents(),
                command.perTransactionLimitCents()
        );

        cards.save(card);
        return new CreateVirtualCardResult(card);
    }

    private static void validate(CreateVirtualCardCommand command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        if (command.fundingAccountId() == null) {
            throw new IllegalArgumentException("funding_account_id is required");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotency_key is required");
        }
    }

    private static String generateLast4() {
        int n = (int) (Math.random() * 10000);
        return String.format("%04d", n);
    }
}
