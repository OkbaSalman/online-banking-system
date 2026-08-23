package com.banking.accounts_service.application.usecase.list_account_invitations.dto;

import com.banking.accounts_service.domain.model.AccountInvitation;

import java.util.List;

public record ListAccountInvitationsResult(List<AccountInvitation> invitations) {}
