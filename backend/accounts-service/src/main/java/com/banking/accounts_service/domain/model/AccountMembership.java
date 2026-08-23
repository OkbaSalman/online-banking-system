package com.banking.accounts_service.domain.model;

import java.util.UUID;

public record AccountMembership(
        UUID accountId,
        UUID userId,
        MembershipRole role,
        long createdAtEpochMs
) {}
