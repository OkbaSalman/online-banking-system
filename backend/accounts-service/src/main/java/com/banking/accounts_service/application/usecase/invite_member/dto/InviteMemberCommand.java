package com.banking.accounts_service.application.usecase.invite_member.dto;

import com.banking.accounts_service.domain.model.MembershipRole;

import java.util.UUID;

public record InviteMemberCommand(
        UUID requesterUserId,
        boolean requesterIsAdmin,
        UUID accountId,
        UUID invitedUserId,
        MembershipRole role,
        Long ttlSeconds,
        String invitedByEmail
) {}
