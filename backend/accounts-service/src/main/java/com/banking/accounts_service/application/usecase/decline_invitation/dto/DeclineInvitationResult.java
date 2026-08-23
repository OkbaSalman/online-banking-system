package com.banking.accounts_service.application.usecase.decline_invitation.dto;

import com.banking.accounts_service.domain.model.AccountInvitation;

public record DeclineInvitationResult(AccountInvitation invitation) {}
