package com.banking.accounts_service.application.usecase.list_my_accounts;

import com.banking.accounts_service.application.usecase.list_my_accounts.dto.ListMyAccountsQuery;
import com.banking.accounts_service.application.usecase.list_my_accounts.dto.ListMyAccountsResult;

public interface ListMyAccountsUseCase {
    ListMyAccountsResult list(ListMyAccountsQuery query);
}
