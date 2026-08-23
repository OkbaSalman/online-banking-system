package com.banking.accounts_service.application.usecase.invite_member.dto;

import com.banking.accounts_service.domain.model.AccountInvitation;

public record InviteMemberResult(AccountInvitation invitation) {}
