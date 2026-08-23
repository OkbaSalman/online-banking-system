package com.banking.ledger_service.application.usecase.list_entries;

import com.banking.ledger_service.application.usecase.list_entries.dto.ListEntriesQuery;
import com.banking.ledger_service.application.usecase.list_entries.dto.ListEntriesResult;

public interface ListEntriesUseCase {
    ListEntriesResult list(ListEntriesQuery query);
}