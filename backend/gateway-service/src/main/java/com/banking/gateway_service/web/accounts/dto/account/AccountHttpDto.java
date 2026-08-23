package com.banking.gateway_service.web.accounts.dto.account;

public record AccountHttpDto(
        String id,
        String iban,
        long createdAtEpochMs,
        String accountType,
        boolean frozen,
        String displayName
) {}
