package com.banking.ledger_service.application.usecase.get_entry;

import com.banking.ledger_service.application.usecase.get_entry.dto.GetEntryQuery;
import com.banking.ledger_service.application.usecase.get_entry.dto.GetEntryResult;

public interface GetEntryUseCase {
    GetEntryResult get(GetEntryQuery query);
}