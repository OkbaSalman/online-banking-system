package com.banking.ledger_service.domain.model;

import java.util.List;
import java.util.UUID;

public record LedgerEntry(
        UUID id,
        UUID initiatorUserId,
        String idempotencyKey,
        String type,
        String description,
        long createdAtEpochMs,
        UUID fromAccountId,
        UUID toAccountId,
        long amountCents,
        List<Posting> postings
) {}