package com.banking.accounts_service.application.usecase.accept_invitation.dto;

import com.banking.accounts_service.domain.model.AccountMembership;

public record AcceptInvitationResult(AccountMembership membership) {}
