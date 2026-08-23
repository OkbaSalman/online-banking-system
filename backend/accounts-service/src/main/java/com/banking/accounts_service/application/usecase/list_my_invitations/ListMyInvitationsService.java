package com.banking.accounts_service.application.usecase.list_my_invitations;

import com.banking.accounts_service.application.port.AccountInvitationsRepositoryPort;
import com.banking.accounts_service.application.usecase.list_my_invitations.dto.ListMyInvitationsQuery;
import com.banking.accounts_service.application.usecase.list_my_invitations.dto.ListMyInvitationsResult;
import com.banking.accounts_service.domain.model.AccountInvitationStatus;

public class ListMyInvitationsService implements ListMyInvitationsUseCase {

    private final AccountInvitationsRepositoryPort invitations;

    public ListMyInvitationsService(AccountInvitationsRepositoryPort invitations) {
        this.invitations = invitations;
    }

    @Override
    public ListMyInvitationsResult list(ListMyInvitationsQuery query) {
        validate(query);

        AccountInvitationStatus status = query.status() == null ? AccountInvitationStatus.PENDING : query.status();
        var res = invitations.listByInvitedUserId(query.requesterUserId(), status, query.limit(), query.offset());
        return new ListMyInvitationsResult(res);
    }

    private static void validate(ListMyInvitationsQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query is required");
        }
        if (query.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
    }
}
