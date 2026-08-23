package com.banking.accounts_service.application.usecase.list_accounts_by_type.dto;

import com.banking.accounts_service.domain.model.AccountType;

public record ListAccountsByTypeQuery(
        AccountType accountType,
        int limit,
        int offset
) {}
