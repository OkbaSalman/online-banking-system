package com.banking.ledger_service.application.port;

import com.banking.ledger_service.domain.model.LedgerEntry;

public record LedgerWriteResult(
        LedgerEntry entry,
        long fromBalanceCents,
        long toBalanceCents
) {}