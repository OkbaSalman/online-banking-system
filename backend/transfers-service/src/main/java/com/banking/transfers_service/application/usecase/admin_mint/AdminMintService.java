package com.banking.transfers_service.application.usecase.admin_mint;

import com.banking.transfers_service.application.port.LedgerClientPort;
import com.banking.transfers_service.application.port.TransferRepositoryPort;
import com.banking.transfers_service.application.usecase.admin_mint.dto.AdminMintCommand;
import com.banking.transfers_service.application.usecase.admin_mint.dto.AdminMintResult;
import com.banking.transfers_service.domain.model.Transfer;
import com.banking.transfers_service.domain.model.TransferStatus;

import java.util.Optional;
import java.util.UUID;

public class AdminMintService implements AdminMintUseCase {

    private final TransferRepositoryPort transfers;
    private final LedgerClientPort ledger;
    private final UUID treasuryAccountId;

    public AdminMintService(TransferRepositoryPort transfers, LedgerClientPort ledger, UUID treasuryAccountId) {
        this.transfers = transfers;
        this.ledger = ledger;
        this.treasuryAccountId = treasuryAccountId;
    }

    @Override
    public AdminMintResult mint(AdminMintCommand command) {
        validate(command);

        Optional<Transfer> existing = transfers.findByInitiatorUserIdAndIdempotencyKey(
                command.initiatorUserId(),
                command.idempotencyKey()
        );
        if (existing.isPresent()) {
            Transfer t = existing.get();
            if (t.ledgerEntryId() != null) {
                long treasuryBal = ledger.getBalanceCents(t.fromAccountId());
                long toBal = ledger.getBalanceCents(t.toAccountId());
                return new AdminMintResult(t, ledger.getEntry(t.ledgerEntryId()), treasuryBal, toBal);
            }
            return new AdminMintResult(t, null, 0L, 0L);
        }

        UUID transferId = UUID.randomUUID();
        long now = System.currentTimeMillis();

        Transfer pending = new Transfer(
                transferId,
                command.initiatorUserId(),
                treasuryAccountId,
                command.toAccountId(),
                command.amountCents(),
                0L,
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
            var ledgerRes = ledger.createTransfer(
                    command.initiatorUserId(),
                    treasuryAccountId,
                    command.toAccountId(),
                    command.amountCents(),
                    command.idempotencyKey(),
                    command.description()
            );

            Transfer completed = new Transfer(
                    transferId,
                    command.initiatorUserId(),
                    treasuryAccountId,
                    command.toAccountId(),
                    command.amountCents(),
                    0L,
                    command.idempotencyKey(),
                    command.description(),
                    now,
                    TransferStatus.COMPLETED,
                    UUID.fromString(ledgerRes.entry().getId()),
                    null,
                    null
            );
            transfers.save(completed);

            return new AdminMintResult(completed, ledgerRes.entry(), ledgerRes.fromBalanceCents(), ledgerRes.toBalanceCents());
        } catch (RuntimeException e) {
            Transfer failed = new Transfer(
                    transferId,
                    command.initiatorUserId(),
                    treasuryAccountId,
                    command.toAccountId(),
                    command.amountCents(),
                    0L,
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

    private static void validate(AdminMintCommand command) {
        if (command.amountCents() <= 0) {
            throw new IllegalArgumentException("amount_cents must be > 0");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotency_key is required");
        }
    }
}
