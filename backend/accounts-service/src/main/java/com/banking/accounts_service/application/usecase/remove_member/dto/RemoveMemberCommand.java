package com.banking.accounts_service.application.usecase.remove_member.dto;

import java.util.UUID;

public record RemoveMemberCommand(
        UUID requesterUserId,
        boolean requesterIsAdmin,
        UUID accountId,
        UUID userIdToRemove
) {}
