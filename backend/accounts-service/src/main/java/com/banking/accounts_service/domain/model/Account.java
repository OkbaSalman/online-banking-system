package com.banking.accounts_service.domain.model;

import java.util.UUID;

public record Account(
        UUID id,
        String iban,
        long createdAtEpochMs,
        AccountType accountType,
        boolean frozen,
        UUID createdByUserId,
        String idempotencyKey,
        String displayName
) {
    public Account {
        displayName = displayName == null ? "" : displayName;
    }
}
