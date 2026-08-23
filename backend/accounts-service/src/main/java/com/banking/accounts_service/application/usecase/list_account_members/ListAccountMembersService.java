package com.banking.accounts_service.application.usecase.list_account_members;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.application.usecase.list_account_members.dto.ListAccountMembersQuery;
import com.banking.accounts_service.application.usecase.list_account_members.dto.ListAccountMembersResult;
public class ListAccountMembersService implements ListAccountMembersUseCase {

    private final AccountsRepositoryPort accounts;

    public ListAccountMembersService(AccountsRepositoryPort accounts) {
        this.accounts = accounts;
    }

    @Override
    public ListAccountMembersResult list(ListAccountMembersQuery query) {
        validate(query);

        accounts.findById(query.accountId()).orElseThrow(() -> new NotFoundException("Account not found"));

        if (!query.requesterIsAdmin()) {
            accounts.findMembership(query.accountId(), query.requesterUserId())
                    .orElseThrow(() -> new ForbiddenException("Not a member of this account"));
            // Any member (or admin) may view the co-owner list; invite/remove stay owner-gated.
        }

        var members = accounts.listMemberships(query.accountId());
        return new ListAccountMembersResult(members);
    }

    private static void validate(ListAccountMembersQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query is required");
        }
        if (query.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
        if (query.accountId() == null) {
            throw new IllegalArgumentException("accountId is required");
        }
    }
}
