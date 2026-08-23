package com.banking.ledger_service.application.usecase.get_entry;

import com.banking.ledger_service.application.port.LedgerRepositoryPort;
import com.banking.ledger_service.application.usecase.get_entry.dto.GetEntryQuery;
import com.banking.ledger_service.application.usecase.get_entry.dto.GetEntryResult;

public class GetEntryService implements GetEntryUseCase {

    private final LedgerRepositoryPort ledger;

    public GetEntryService(LedgerRepositoryPort ledger) {
        this.ledger = ledger;
    }

    @Override
    public GetEntryResult get(GetEntryQuery query) {
        return new GetEntryResult(ledger.getEntry(query.entryId()));
    }
}