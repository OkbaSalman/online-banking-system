package com.banking.accounts_service.application.usecase.cancel_invitation.dto;

import com.banking.accounts_service.domain.model.AccountInvitation;

public record CancelInvitationResult(AccountInvitation invitation) {}
