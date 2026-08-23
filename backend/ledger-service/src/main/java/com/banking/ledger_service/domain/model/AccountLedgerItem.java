package com.banking.ledger_service.domain.model;

import java.util.UUID;

public record AccountLedgerItem(
        UUID id,
        UUID accountId,
        UUID entryId,
        long createdAtEpochMs,
        long amountCents,
        UUID counterpartyAccountId,
        long seq,
        String prevHash,
        String itemHash,
        LedgerEntry entry
) {}
