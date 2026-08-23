package com.banking.accounts_service.application.usecase.list_account_invitations;

import com.banking.accounts_service.application.port.AccountInvitationsRepositoryPort;
import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.application.usecase.list_account_invitations.dto.ListAccountInvitationsQuery;
import com.banking.accounts_service.application.usecase.list_account_invitations.dto.ListAccountInvitationsResult;
import com.banking.accounts_service.domain.model.AccountInvitationStatus;
import com.banking.accounts_service.domain.model.MembershipRole;

public class ListAccountInvitationsService implements ListAccountInvitationsUseCase {

    private final AccountsRepositoryPort accounts;
    private final AccountInvitationsRepositoryPort invitations;

    public ListAccountInvitationsService(AccountsRepositoryPort accounts, AccountInvitationsRepositoryPort invitations) {
        this.accounts = accounts;
        this.invitations = invitations;
    }

    @Override
    public ListAccountInvitationsResult list(ListAccountInvitationsQuery query) {
        validate(query);

        accounts.findById(query.accountId()).orElseThrow(() -> new NotFoundException("Account not found"));

        if (!query.requesterIsAdmin()) {
            var requesterMembership = accounts.findMembership(query.accountId(), query.requesterUserId())
                    .orElseThrow(() -> new ForbiddenException("Not a member of this account"));
            if (requesterMembership.role() != MembershipRole.OWNER) {
                throw new ForbiddenException("OWNER role required to list invitations");
            }
        }

        AccountInvitationStatus status = query.status() == null ? AccountInvitationStatus.PENDING : query.status();
        var res = invitations.listByAccountId(query.accountId(), status, query.limit(), query.offset());
        return new ListAccountInvitationsResult(res);
    }

    private static void validate(ListAccountInvitationsQuery query) {
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
