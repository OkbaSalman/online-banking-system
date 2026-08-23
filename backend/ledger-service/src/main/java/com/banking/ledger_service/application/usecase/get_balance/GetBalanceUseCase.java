package com.banking.ledger_service.application.usecase.get_balance;

import com.banking.ledger_service.application.usecase.get_balance.dto.GetBalanceQuery;
import com.banking.ledger_service.application.usecase.get_balance.dto.GetBalanceResult;

public interface GetBalanceUseCase {
    GetBalanceResult get(GetBalanceQuery query);
}