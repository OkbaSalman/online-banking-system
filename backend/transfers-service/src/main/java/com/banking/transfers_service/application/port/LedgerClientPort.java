package com.banking.transfers_service.application.port;

import com.banking.ledger.v1.LedgerEntry;

import java.util.UUID;

public interface LedgerClientPort {

    LedgerTransferResult createTransfer(
            UUID initiatorUserId,
            UUID fromAccountId,
            UUID toAccountId,
            long amountCents,
            String idempotencyKey,
            String description
    );

    LedgerEntry getEntry(UUID entryId);

    long getBalanceCents(UUID accountId);
}
