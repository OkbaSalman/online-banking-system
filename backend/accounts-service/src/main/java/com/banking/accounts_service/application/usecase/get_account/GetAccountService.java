package com.banking.accounts_service.application.usecase.get_account;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.application.usecase.get_account.dto.GetAccountQuery;
import com.banking.accounts_service.application.usecase.get_account.dto.GetAccountResult;

public class GetAccountService implements GetAccountUseCase {

    private final AccountsRepositoryPort accounts;

    public GetAccountService(AccountsRepositoryPort accounts) {
        this.accounts = accounts;
    }

    @Override
    public GetAccountResult get(GetAccountQuery query) {
        validate(query);

        var account = accounts.findById(query.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!query.requesterIsAdmin()) {
            boolean isMember = accounts.findMembership(query.accountId(), query.requesterUserId()).isPresent();
            if (!isMember) {
                throw new ForbiddenException("Not a member of this account");
            }
        }

        return new GetAccountResult(account);
    }

    private static void validate(GetAccountQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query is required");
        }
        if (query.accountId() == null) {
            throw new IllegalArgumentException("accountId is required");
        }
        if (query.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
    }
}
