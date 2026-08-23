package com.banking.transfers_service.application.usecase.create_transfer.dto;

import com.banking.ledger.v1.LedgerEntry;
import com.banking.transfers_service.domain.model.Transfer;

public record CreateTransferResult(
        Transfer transfer,
        LedgerEntry ledgerEntry,
        long fromBalanceCents,
        long toBalanceCents
) {}
