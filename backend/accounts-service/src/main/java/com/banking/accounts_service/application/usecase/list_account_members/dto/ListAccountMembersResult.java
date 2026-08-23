package com.banking.accounts_service.application.usecase.list_account_members.dto;

import com.banking.accounts_service.domain.model.AccountMembership;

import java.util.List;

public record ListAccountMembersResult(List<AccountMembership> members) {}
