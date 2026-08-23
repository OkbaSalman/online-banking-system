package com.banking.gateway_service.web.accounts.dto.create;

public record CreateAccountHttpRequest(
        String idempotencyKey,
        String accountType,
        String displayName
) {}
