package com.banking.accounts_service.application.usecase.create_account.dto;

import com.banking.accounts_service.domain.model.AccountType;

import java.util.UUID;

public record CreateAccountCommand(
        UUID requesterUserId,
        String idempotencyKey,
        AccountType accountType,
        String displayName
) {}
