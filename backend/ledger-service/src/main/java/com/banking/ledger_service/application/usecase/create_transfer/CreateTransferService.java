package com.banking.ledger_service.application.usecase.create_transfer;

import com.banking.ledger_service.application.port.LedgerRepositoryPort;
import com.banking.ledger_service.application.usecase.create_transfer.dto.CreateTransferCommand;
import com.banking.ledger_service.application.usecase.create_transfer.dto.CreateTransferResult;

public class CreateTransferService implements CreateTransferUseCase {

    private final LedgerRepositoryPort ledger;

    public CreateTransferService(LedgerRepositoryPort ledger) {
        this.ledger = ledger;
    }

    @Override
    public CreateTransferResult create(CreateTransferCommand command) {
        if (command.amountCents() <= 0) {
            throw new IllegalArgumentException("amount_cents must be > 0");
        }
        if (command.fromAccountId().equals(command.toAccountId())) {
            throw new IllegalArgumentException("from_account_id must differ from to_account_id");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotency_key is required");
        }

        var res = ledger.createTransfer(
                command.userId(),
                command.fromAccountId(),
                command.toAccountId(),
                command.amountCents(),
                command.idempotencyKey(),
                command.description()
        );

        return new CreateTransferResult(res.entry(), res.fromBalanceCents(), res.toBalanceCents());
    }
}