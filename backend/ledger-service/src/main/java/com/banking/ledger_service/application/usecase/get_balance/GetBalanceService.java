package com.banking.ledger_service.application.usecase.get_balance;

import com.banking.ledger_service.application.port.LedgerRepositoryPort;
import com.banking.ledger_service.application.usecase.get_balance.dto.GetBalanceQuery;
import com.banking.ledger_service.application.usecase.get_balance.dto.GetBalanceResult;

public class GetBalanceService implements GetBalanceUseCase {

    private final LedgerRepositoryPort ledger;

    public GetBalanceService(LedgerRepositoryPort ledger) {
        this.ledger = ledger;
    }

    @Override
    public GetBalanceResult get(GetBalanceQuery query) {
        var balance = ledger.getBalance(query.accountId());
        return new GetBalanceResult(balance.accountId(), balance.availableCents());
    }
}