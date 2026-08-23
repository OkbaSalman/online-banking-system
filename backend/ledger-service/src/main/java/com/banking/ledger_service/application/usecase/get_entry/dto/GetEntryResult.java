package com.banking.ledger_service.application.usecase.get_entry.dto;

import com.banking.ledger_service.domain.model.LedgerEntry;

public record GetEntryResult(LedgerEntry entry) {}