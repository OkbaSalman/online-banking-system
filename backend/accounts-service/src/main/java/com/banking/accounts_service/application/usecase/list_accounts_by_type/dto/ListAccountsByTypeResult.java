package com.banking.accounts_service.application.usecase.list_accounts_by_type.dto;

import com.banking.accounts_service.domain.model.Account;

import java.util.List;

public record ListAccountsByTypeResult(
        List<Account> accounts
) {}
