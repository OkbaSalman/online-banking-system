package com.banking.accounts_service.application.usecase.is_member.dto;

import com.banking.accounts_service.domain.model.MembershipRole;

public record IsMemberResult(
        boolean isMember,
        MembershipRole role
) {}
