package com.banking.gateway_service.web.accounts.dto.membership;

public record AccountMembershipHttpDto(
        String accountId,
        String userId,
        String role,
        long createdAtEpochMs
) {}
