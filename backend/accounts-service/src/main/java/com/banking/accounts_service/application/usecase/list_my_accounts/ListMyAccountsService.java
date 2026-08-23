package com.banking.accounts_service.application.usecase.list_my_accounts;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.list_my_accounts.dto.ListMyAccountsQuery;
import com.banking.accounts_service.application.usecase.list_my_accounts.dto.ListMyAccountsResult;

public class ListMyAccountsService implements ListMyAccountsUseCase {

    private final AccountsRepositoryPort accounts;

    public ListMyAccountsService(AccountsRepositoryPort accounts) {
        this.accounts = accounts;
    }

    @Override
    public ListMyAccountsResult list(ListMyAccountsQuery query) {
        if (query == null || query.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
        return new ListMyAccountsResult(accounts.listAccountsByUserId(query.requesterUserId()));
    }
}
