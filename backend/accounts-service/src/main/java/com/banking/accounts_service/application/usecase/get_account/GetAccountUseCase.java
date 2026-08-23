package com.banking.accounts_service.application.usecase.get_account;

import com.banking.accounts_service.application.usecase.get_account.dto.GetAccountQuery;
import com.banking.accounts_service.application.usecase.get_account.dto.GetAccountResult;

public interface GetAccountUseCase {
    GetAccountResult get(GetAccountQuery query);
}
