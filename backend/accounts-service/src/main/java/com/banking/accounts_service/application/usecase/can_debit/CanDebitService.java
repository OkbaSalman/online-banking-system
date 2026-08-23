package com.banking.accounts_service.application.usecase.can_debit;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.can_debit.dto.CanDebitQuery;
import com.banking.accounts_service.application.usecase.can_debit.dto.CanDebitResult;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.domain.model.AccountType;

public class CanDebitService implements CanDebitUseCase {

    private final AccountsRepositoryPort accounts;

    public CanDebitService(AccountsRepositoryPort accounts) {
        this.accounts = accounts;
    }

    @Override
    public CanDebitResult canDebit(CanDebitQuery query) {
        validate(query);

        if (!query.requesterIsAdmin() && !query.requesterUserId().equals(query.userId())) {
            throw new ForbiddenException("Not allowed to query debit permission for another user");
        }

        var account = accounts.findById(query.accountId()).orElse(null);
        AccountType accountType = account == null ? null : account.accountType();

        if (account != null && account.frozen()) {
            return new CanDebitResult(false, "Account is frozen", accountType);
        }

        boolean member = accounts.findMembership(query.accountId(), query.userId()).isPresent();
        if (!member) {
            return new CanDebitResult(false, "User is not a member of this account", accountType);
        }
        return new CanDebitResult(true, "", accountType);
    }

    private static void validate(CanDebitQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query is required");
        }
        if (query.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
        if (query.accountId() == null) {
            throw new IllegalArgumentException("accountId is required");
        }
        if (query.userId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
    }
}
