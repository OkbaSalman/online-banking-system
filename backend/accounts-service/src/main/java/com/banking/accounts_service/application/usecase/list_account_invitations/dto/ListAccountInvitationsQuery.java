package com.banking.accounts_service.application.usecase.list_account_invitations.dto;

import com.banking.accounts_service.domain.model.AccountInvitationStatus;

import java.util.UUID;

public record ListAccountInvitationsQuery(
        UUID requesterUserId,
        boolean requesterIsAdmin,
        UUID accountId,
        AccountInvitationStatus status,
        int limit,
        int offset
) {}
