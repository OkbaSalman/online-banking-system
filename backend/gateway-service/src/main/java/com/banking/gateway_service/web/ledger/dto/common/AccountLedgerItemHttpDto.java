package com.banking.gateway_service.web.ledger.dto.common;

public record AccountLedgerItemHttpDto(
        String id,
        String accountId,
        String entryId,
        long createdAtEpochMs,
        long amountCents,
        String counterpartyAccountId,
        long seq,
        String prevHash,
        String itemHash,
        LedgerEntryHttpDto entry
) {}
