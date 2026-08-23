package com.banking.accounts_service.application.usecase.is_member.dto;

import java.util.UUID;

public record IsMemberQuery(
        UUID requesterUserId,
        boolean requesterIsAdmin,
        UUID accountId,
        UUID userId
) {}
