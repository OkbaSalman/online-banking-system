package com.banking.accounts_service.application.usecase.list_accounts_by_type;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.list_accounts_by_type.dto.ListAccountsByTypeQuery;
import com.banking.accounts_service.application.usecase.list_accounts_by_type.dto.ListAccountsByTypeResult;

public class ListAccountsByTypeService implements ListAccountsByTypeUseCase {

    private final AccountsRepositoryPort accounts;

    public ListAccountsByTypeService(AccountsRepositoryPort accounts) {
        this.accounts = accounts;
    }

    @Override
    public ListAccountsByTypeResult list(ListAccountsByTypeQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query is required");
        }
        if (query.accountType() == null) {
            throw new IllegalArgumentException("accountType is required");
        }
        return new ListAccountsByTypeResult(accounts.listAccountsByType(query.accountType(), query.limit(), query.offset()));
    }
}
