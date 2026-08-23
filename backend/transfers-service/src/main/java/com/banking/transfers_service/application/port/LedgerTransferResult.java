package com.banking.transfers_service.application.port;

import com.banking.ledger.v1.LedgerEntry;

public record LedgerTransferResult(
        LedgerEntry entry,
        long fromBalanceCents,
        long toBalanceCents
) {}
