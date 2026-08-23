package com.banking.accounts_service.domain.model;

import java.util.UUID;

public record AccountInvitation(
        UUID id,
        UUID accountId,
        UUID invitedUserId,
        UUID invitedByUserId,
        MembershipRole role,
        AccountInvitationStatus status,
        long createdAtEpochMs,
        long expiresAtEpochMs,
        Long respondedAtEpochMs,
        String invitedByEmail
) {
    public AccountInvitation {
        invitedByEmail = invitedByEmail == null ? "" : invitedByEmail;
    }
}
