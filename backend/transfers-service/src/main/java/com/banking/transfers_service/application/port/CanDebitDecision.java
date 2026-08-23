package com.banking.transfers_service.application.port;

public record CanDebitDecision(
        boolean allowed,
        String reason,
        AccountType accountType
) {}
