package com.banking.accounts_service.application.usecase.list_account_invitations;

import com.banking.accounts_service.application.usecase.list_account_invitations.dto.ListAccountInvitationsQuery;
import com.banking.accounts_service.application.usecase.list_account_invitations.dto.ListAccountInvitationsResult;

public interface ListAccountInvitationsUseCase {
    ListAccountInvitationsResult list(ListAccountInvitationsQuery query);
}
