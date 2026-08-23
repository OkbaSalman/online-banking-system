package com.banking.cards_service.application.usecase.charge_card;

import com.banking.cards_service.application.port.CardChargeQueryPort;
import com.banking.cards_service.application.port.CardChargeRepositoryPort;
import com.banking.cards_service.application.port.CardRepositoryPort;
import com.banking.cards_service.application.port.KycClientPort;
import com.banking.cards_service.application.port.KycStatus;
import com.banking.cards_service.application.port.TransfersClientPort;
import com.banking.cards_service.application.usecase.charge_card.dto.ChargeCardCommand;
import com.banking.cards_service.application.usecase.charge_card.dto.ChargeCardResult;
import com.banking.cards_service.application.usecase.common.exception.NotFoundException;
import com.banking.cards_service.domain.model.Card;
import com.banking.cards_service.domain.model.CardCharge;
import com.banking.cards_service.domain.model.CardChargeStatus;
import com.banking.cards_service.domain.model.CardStatus;
import com.banking.transfers.v1.TransferStatus;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ChargeCardService implements ChargeCardUseCase {

    private static final List<String> COUNTED_STATUSES = List.of(
            CardChargeStatus.COMPLETED.name(),
            CardChargeStatus.PENDING.name()
    );

    private final CardRepositoryPort cards;
    private final CardChargeRepositoryPort charges;
    private final CardChargeQueryPort chargeQuery;
    private final TransfersClientPort transfers;
    private final KycClientPort kyc;

    public ChargeCardService(
            CardRepositoryPort cards,
            CardChargeRepositoryPort charges,
            CardChargeQueryPort chargeQuery,
            TransfersClientPort transfers,
            KycClientPort kyc
    ) {
        this.cards = cards;
        this.charges = charges;
        this.chargeQuery = chargeQuery;
        this.transfers = transfers;
        this.kyc = kyc;
    }

    @Override
    public ChargeCardResult charge(ChargeCardCommand command) {
        validate(command);

        Optional<CardCharge> existing = charges.findByUserIdAndIdempotencyKey(command.userId(), command.idempotencyKey());
        if (existing.isPresent()) {
            return new ChargeCardResult(existing.get());
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
        if (card.status() != CardStatus.ACTIVE) {
            throw new IllegalArgumentException("Card not active");
        }

        enforceLimits(card, command.amountCents());

        long now = System.currentTimeMillis();

        CardCharge pending = new CardCharge(
                UUID.randomUUID(),
                command.userId(),
                card.id(),
                command.merchantAccountId(),
                command.amountCents(),
                now,
                CardChargeStatus.PENDING,
                command.idempotencyKey(),
                command.description(),
                null,
                null,
                0L
        );

        charges.save(pending);

        String transferIdem = "cardcharge:" + pending.id();

        try {
            var transfer = transfers.createTransfer(
                    card.fundingAccountId(),
                    command.merchantAccountId(),
                    command.amountCents(),
                    transferIdem,
                    command.description()
            );

            CardChargeStatus finalStatus = switch (transfer.getStatus()) {
                case TRANSFER_STATUS_COMPLETED -> CardChargeStatus.COMPLETED;
                case TRANSFER_STATUS_BLOCKED -> CardChargeStatus.BLOCKED;
                case TRANSFER_STATUS_FAILED -> CardChargeStatus.FAILED;
                case TRANSFER_STATUS_PENDING, TRANSFER_STATUS_UNSPECIFIED, UNRECOGNIZED -> CardChargeStatus.FAILED;
            };

            CardCharge completed = new CardCharge(
                    pending.id(),
                    pending.userId(),
                    pending.cardId(),
                    pending.merchantAccountId(),
                    pending.amountCents(),
                    pending.createdAtEpochMs(),
                    finalStatus,
                    pending.idempotencyKey(),
                    pending.description(),
                    UUID.fromString(transfer.getId()),
                    transfer.getFailureMessage().isBlank() ? null : transfer.getFailureMessage(),
                    transfer.getFeeCents()
            );

            charges.save(completed);
            return new ChargeCardResult(completed);
        } catch (Throwable t) {
            String message = t.getMessage();
            if (t instanceof io.grpc.StatusRuntimeException sre && sre.getStatus().getDescription() != null) {
                message = sre.getStatus().getDescription();
            } else if (t instanceof io.grpc.StatusException se && se.getStatus().getDescription() != null) {
                message = se.getStatus().getDescription();
            }

            CardCharge failed = new CardCharge(
                    pending.id(),
                    pending.userId(),
                    pending.cardId(),
                    pending.merchantAccountId(),
                    pending.amountCents(),
                    pending.createdAtEpochMs(),
                    CardChargeStatus.FAILED,
                    pending.idempotencyKey(),
                    pending.description(),
                    pending.transferId(),
                    message,
                    0L
            );

            charges.save(failed);
            return new ChargeCardResult(failed);
        }
    }

    private void enforceLimits(Card card, long amountCents) {
        if (card.perTransactionLimitCents() > 0 && amountCents > card.perTransactionLimitCents()) {
            throw new IllegalArgumentException("Amount exceeds this card's per-transaction limit");
        }

        long now = System.currentTimeMillis();
        ZonedDateTime zdt = Instant.ofEpochMilli(now).atZone(ZoneOffset.UTC);
        long dayStart = zdt.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        long monthStart = zdt.withDayOfMonth(1).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        if (card.dailyLimitCents() > 0) {
            long spentToday = chargeQuery.sumAmountSince(card.id(), COUNTED_STATUSES, dayStart);
            if (spentToday + amountCents > card.dailyLimitCents()) {
                throw new IllegalArgumentException("Amount exceeds this card's daily limit");
            }
        }

        if (card.monthlyLimitCents() > 0) {
            long spentThisMonth = chargeQuery.sumAmountSince(card.id(), COUNTED_STATUSES, monthStart);
            if (spentThisMonth + amountCents > card.monthlyLimitCents()) {
                throw new IllegalArgumentException("Amount exceeds this card's monthly limit");
            }
        }
    }

    private static void validate(ChargeCardCommand command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        if (command.cardId() == null) {
            throw new IllegalArgumentException("card_id is required");
        }
        if (command.merchantAccountId() == null) {
            throw new IllegalArgumentException("merchant_account_id is required");
        }
        if (command.amountCents() <= 0) {
            throw new IllegalArgumentException("amount_cents must be > 0");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotency_key is required");
        }
    }
}
