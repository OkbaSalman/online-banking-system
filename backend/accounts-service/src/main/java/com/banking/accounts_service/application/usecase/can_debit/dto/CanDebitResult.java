package com.banking.accounts_service.application.usecase.can_debit.dto;

import com.banking.accounts_service.domain.model.AccountType;

public record CanDebitResult(
        boolean allowed,
        String reason,
        AccountType accountType
) {}
