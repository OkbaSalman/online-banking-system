package com.banking.ledger_service.application.usecase.create_transfer.dto;

import com.banking.ledger_service.domain.model.LedgerEntry;

public record CreateTransferResult(
        LedgerEntry entry,
        long fromBalanceCents,
        long toBalanceCents
) {}