package com.banking.accounts_service.application.usecase.list_my_invitations.dto;

import com.banking.accounts_service.domain.model.AccountInvitationStatus;

import java.util.UUID;

public record ListMyInvitationsQuery(
        UUID requesterUserId,
        AccountInvitationStatus status,
        int limit,
        int offset
) {}
