package com.banking.transfers_service.application.usecase.create_transfer;

import com.banking.transfers_service.application.port.AccountType;
import com.banking.transfers_service.application.port.AccountsClientPort;
import com.banking.transfers_service.application.port.AmlPort;
import com.banking.transfers_service.application.port.AmlDecision;
import com.banking.transfers_service.application.port.KycClientPort;
import com.banking.transfers_service.application.port.KycStatus;
import com.banking.transfers_service.application.port.LedgerClientPort;
import com.banking.transfers_service.application.port.TransferQueryPort;
import com.banking.transfers_service.application.port.TransferRepositoryPort;
import com.banking.transfers_service.application.usecase.create_transfer.dto.CreateTransferCommand;
import com.banking.transfers_service.application.usecase.create_transfer.dto.CreateTransferResult;
import com.banking.transfers_service.domain.model.Transfer;
import com.banking.transfers_service.domain.model.TransferStatus;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class CreateTransferService implements CreateTransferUseCase {

    private final TransferRepositoryPort transfers;
    private final LedgerClientPort ledger;
    private final AmlPort aml;
    private final TransferQueryPort transferQueryPort;
    private final AccountsClientPort accounts;
    private final KycClientPort kyc;
    private final UUID revenueAccountId;
    private final long feeBps;
    private final long savingsMaxDebitsPerMonth;

    public CreateTransferService(
            TransferRepositoryPort transfers,
            LedgerClientPort ledger,
            AmlPort aml,
            TransferQueryPort transferQueryPort,
            AccountsClientPort accounts,
            KycClientPort kyc,
            UUID revenueAccountId,
            long feeBps,
            long savingsMaxDebitsPerMonth
    ) {
        this.transfers = transfers;
        this.ledger = ledger;
        this.aml = aml;
        this.transferQueryPort = transferQueryPort;
        this.accounts = accounts;
        this.kyc = kyc;
        this.revenueAccountId = revenueAccountId;
        this.feeBps = feeBps;
        this.savingsMaxDebitsPerMonth = savingsMaxDebitsPerMonth;
    }

    @Override
    public CreateTransferResult create(CreateTransferCommand command) {
        validate(command);

        Optional<Transfer> existing = transfers.findByInitiatorUserIdAndIdempotencyKey(
                command.initiatorUserId(),
                command.idempotencyKey()
        );
        if (existing.isPresent()) {
            Transfer t = existing.get();
            if (t.ledgerEntryId() != null && t.status() == TransferStatus.COMPLETED) {
                long fromBal = ledger.getBalanceCents(t.fromAccountId());
                long toBal = ledger.getBalanceCents(t.toAccountId());
                return new CreateTransferResult(t, ledger.getEntry(t.ledgerEntryId()), fromBal, toBal);
            }
            return new CreateTransferResult(t, null, 0L, 0L);
        }

        UUID transferId = UUID.randomUUID();
        long now = System.currentTimeMillis();

        KycStatus kycStatus = kyc.getMyKycStatus();
        if (kycStatus != KycStatus.APPROVED) {
            Transfer blocked = new Transfer(
                    transferId,
                    command.initiatorUserId(),
                    command.fromAccountId(),
                    command.toAccountId(),
                    command.amountCents(),
                    0L,
                    command.idempotencyKey(),
                    command.description(),
                    now,
                    TransferStatus.BLOCKED,
                    null,
                    null,
                    "KYC not approved"
            );
            transfers.save(blocked);
            return new CreateTransferResult(blocked, null, 0L, 0L);
        }

        var debitDecision = accounts.canDebit(command.fromAccountId(), command.initiatorUserId());
        if (!debitDecision.allowed()) {
            Transfer blocked = new Transfer(
                    transferId,
                    command.initiatorUserId(),
                    command.fromAccountId(),
                    command.toAccountId(),
                    command.amountCents(),
                    0L,
                    command.idempotencyKey(),
                    command.description(),
                    now,
                    TransferStatus.BLOCKED,
                    null,
                    null,
                    debitDecision.reason() == null || debitDecision.reason().isBlank() ? "Not allowed" : debitDecision.reason()
            );
            transfers.save(blocked);
            return new CreateTransferResult(blocked, null, 0L, 0L);
        }

        AccountType fromType = debitDecision.accountType();
        if (fromType == AccountType.SAVINGS) {
            long monthStart = startOfCurrentMonthEpochMs();
            long used = transferQueryPort.countRecentCompletedDebits(command.fromAccountId(), monthStart);
            if (used >= savingsMaxDebitsPerMonth) {
                Transfer blocked = new Transfer(
                        transferId,
                        command.initiatorUserId(),
                        command.fromAccountId(),
                        command.toAccountId(),
                        command.amountCents(),
                        0L,
                        command.idempotencyKey(),
                        command.description(),
                        now,
                        TransferStatus.BLOCKED,
                        null,
                        null,
                        "Savings monthly transaction limit reached"
                );
                transfers.save(blocked);
                return new CreateTransferResult(blocked, null, 0L, 0L);
            }
        }

        long feeCents = calculateFeeCents(command.amountCents(), feeBps);
        long needed = command.amountCents() + feeCents;
        long fromBefore = ledger.getBalanceCents(command.fromAccountId());
        if (fromBefore < needed) {
            throw new IllegalArgumentException("Insufficient funds for amount + fee");
        }

        AmlDecision amlDecision = aml.isTransferAllowed(
                command.initiatorUserId(),
                command.fromAccountId(),
                command.toAccountId(),
                command.amountCents()
        );

        if (!amlDecision.allowed()) {
            Transfer blocked = new Transfer(
                    transferId,
                    command.initiatorUserId(),
                    command.fromAccountId(),
                    command.toAccountId(),
                    command.amountCents(),
                    0L,
                    command.idempotencyKey(),
                    command.description(),
                    now,
                    TransferStatus.BLOCKED,
                    null,
                    null,
                    amlDecision.reason() == null || amlDecision.reason().isBlank() ? "Blocked by AML" : amlDecision.reason()
            );
            transfers.save(blocked);
            return new CreateTransferResult(blocked, null, 0L, 0L);
        }

        Transfer pending = new Transfer(
                transferId,
                command.initiatorUserId(),
                command.fromAccountId(),
                command.toAccountId(),
                command.amountCents(),
                feeCents,
                command.idempotencyKey(),
                command.description(),
                now,
                TransferStatus.PENDING,
                null,
                null,
                null
        );
        transfers.save(pending);

        try {
            String ledgerMainIdem = command.idempotencyKey() + ":main";
            var ledgerRes = ledger.createTransfer(
                    command.initiatorUserId(),
                    command.fromAccountId(),
                    command.toAccountId(),
                    command.amountCents(),
                    ledgerMainIdem,
                    command.description()
            );

            UUID mainEntryId = UUID.fromString(ledgerRes.entry().getId());
            UUID feeEntryId = null;

            if (feeCents > 0) {
                String ledgerFeeIdem = command.idempotencyKey() + ":fee";
                String feeDesc = "fee: " + (command.description() == null ? "" : command.description());
                var feeRes = ledger.createTransfer(
                        command.initiatorUserId(),
                        command.fromAccountId(),
                        revenueAccountId,
                        feeCents,
                        ledgerFeeIdem,
                        feeDesc
                );
                feeEntryId = UUID.fromString(feeRes.entry().getId());
            }

            Transfer completed = new Transfer(
                    transferId,
                    command.initiatorUserId(),
                    command.fromAccountId(),
                    command.toAccountId(),
                    command.amountCents(),
                    feeCents,
                    command.idempotencyKey(),
                    command.description(),
                    now,
                    TransferStatus.COMPLETED,
                    mainEntryId,
                    feeEntryId,
                    null
            );
            transfers.save(completed);

            long fromBal = ledger.getBalanceCents(command.fromAccountId());
            long toBal = ledger.getBalanceCents(command.toAccountId());
            return new CreateTransferResult(completed, ledgerRes.entry(), fromBal, toBal);
        } catch (RuntimeException e) {
            Transfer failed = new Transfer(
                    transferId,
                    command.initiatorUserId(),
                    command.fromAccountId(),
                    command.toAccountId(),
                    command.amountCents(),
                    feeCents,
                    command.idempotencyKey(),
                    command.description(),
                    now,
                    TransferStatus.FAILED,
                    null,
                    null,
                    e.getMessage()
            );
            transfers.save(failed);
            throw e;
        }
    }

    private static void validate(CreateTransferCommand command) {
        if (command.amountCents() <= 0) {
            throw new IllegalArgumentException("amount_cents must be > 0");
        }
        if (command.fromAccountId().equals(command.toAccountId())) {
            throw new IllegalArgumentException("from_account_id must differ from to_account_id");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotency_key is required");
        }
    }

    private static long calculateFeeCents(long amountCents, long feeBps) {
        if (feeBps <= 0) {
            return 0L;
        }
        long numerator = amountCents * feeBps;
        return (numerator + 9999L) / 10000L;
    }

    private static long startOfCurrentMonthEpochMs() {
        return ZonedDateTime.now(ZoneOffset.UTC)
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli();
    }
}
