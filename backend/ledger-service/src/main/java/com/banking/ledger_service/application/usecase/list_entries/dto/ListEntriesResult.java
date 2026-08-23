package com.banking.ledger_service.application.usecase.list_entries.dto;

import com.banking.ledger_service.domain.model.AccountLedgerItem;

import java.util.List;

public record ListEntriesResult(List<AccountLedgerItem> items) {}