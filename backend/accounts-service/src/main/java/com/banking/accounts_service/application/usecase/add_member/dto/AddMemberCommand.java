package com.banking.accounts_service.application.usecase.add_member.dto;

import com.banking.accounts_service.domain.model.MembershipRole;

import java.util.UUID;

public record AddMemberCommand(
        UUID requesterUserId,
        boolean requesterIsAdmin,
        UUID accountId,
        UUID memberUserId,
        MembershipRole role
) {}
