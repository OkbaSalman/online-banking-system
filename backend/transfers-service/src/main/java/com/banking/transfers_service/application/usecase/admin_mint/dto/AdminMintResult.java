package com.banking.transfers_service.application.usecase.admin_mint.dto;

import com.banking.ledger.v1.LedgerEntry;
import com.banking.transfers_service.domain.model.Transfer;

public record AdminMintResult(
        Transfer transfer,
        LedgerEntry ledgerEntry,
        long treasuryBalanceCents,
        long toBalanceCents
) {}
