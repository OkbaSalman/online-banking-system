package com.banking.ledger_service.application.usecase.list_entries;

import com.banking.ledger_service.application.port.LedgerRepositoryPort;
import com.banking.ledger_service.application.usecase.list_entries.dto.ListEntriesQuery;
import com.banking.ledger_service.application.usecase.list_entries.dto.ListEntriesResult;

public class ListEntriesService implements ListEntriesUseCase {

    private final LedgerRepositoryPort ledger;

    public ListEntriesService(LedgerRepositoryPort ledger) {
        this.ledger = ledger;
    }

    @Override
    public ListEntriesResult list(ListEntriesQuery query) {

        if (query.limit() < 1 || query.limit() > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }
        if (query.offset() < 0) {
            throw new IllegalArgumentException("offset must be >= 0");
        }
        return new ListEntriesResult(ledger.listAccountEntries(query.accountId(), query.limit(), query.offset()));
    }
}