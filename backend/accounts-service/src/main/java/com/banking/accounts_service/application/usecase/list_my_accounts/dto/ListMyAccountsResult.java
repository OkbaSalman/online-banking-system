package com.banking.accounts_service.application.usecase.list_my_accounts.dto;

import com.banking.accounts_service.domain.model.Account;

import java.util.List;

public record ListMyAccountsResult(List<Account> accounts) {}
