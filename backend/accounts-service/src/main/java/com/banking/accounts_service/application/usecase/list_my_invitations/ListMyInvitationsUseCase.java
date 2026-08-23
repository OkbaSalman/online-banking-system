package com.banking.accounts_service.application.usecase.list_my_invitations;

import com.banking.accounts_service.application.usecase.list_my_invitations.dto.ListMyInvitationsQuery;
import com.banking.accounts_service.application.usecase.list_my_invitations.dto.ListMyInvitationsResult;

public interface ListMyInvitationsUseCase {
    ListMyInvitationsResult list(ListMyInvitationsQuery query);
}
