package com.banking.accounts_service.application.usecase.is_member;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.is_member.dto.IsMemberQuery;
import com.banking.accounts_service.application.usecase.is_member.dto.IsMemberResult;

public class IsMemberService implements IsMemberUseCase {

    private final AccountsRepositoryPort accounts;

    public IsMemberService(AccountsRepositoryPort accounts) {
        this.accounts = accounts;
    }

    @Override
    public IsMemberResult isMember(IsMemberQuery query) {
        validate(query);

        if (!query.requesterIsAdmin() && !query.requesterUserId().equals(query.userId())) {
            throw new ForbiddenException("Not allowed to query membership for another user");
        }

        var membership = accounts.findMembership(query.accountId(), query.userId());
        if (membership.isEmpty()) {
            return new IsMemberResult(false, null);
        }
        return new IsMemberResult(true, membership.get().role());
    }

    private static void validate(IsMemberQuery query) {
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
