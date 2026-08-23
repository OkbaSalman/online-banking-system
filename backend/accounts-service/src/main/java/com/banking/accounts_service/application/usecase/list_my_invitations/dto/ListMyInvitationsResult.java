package com.banking.accounts_service.application.usecase.list_my_invitations.dto;

import com.banking.accounts_service.domain.model.AccountInvitation;

import java.util.List;

public record ListMyInvitationsResult(List<AccountInvitation> invitations) {}
