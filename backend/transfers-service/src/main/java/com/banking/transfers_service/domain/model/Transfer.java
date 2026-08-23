package com.banking.transfers_service.domain.model;

import java.util.UUID;

public record Transfer(
        UUID id,
        UUID initiatorUserId,
        UUID fromAccountId,
        UUID toAccountId,
        long amountCents,
        long feeCents,
        String idempotencyKey,
        String description,
        long createdAtEpochMs,
        TransferStatus status,
        UUID ledgerEntryId,
        UUID feeLedgerEntryId,
        String failureMessage
) {}
