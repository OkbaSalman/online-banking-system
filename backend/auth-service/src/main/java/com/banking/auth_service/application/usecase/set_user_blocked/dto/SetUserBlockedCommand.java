package com.banking.auth_service.application.usecase.set_user_blocked.dto;

import java.util.UUID;

public record SetUserBlockedCommand(
        UUID requesterUserId,
        boolean requesterIsAdmin,
        UUID userId,
        boolean blocked
) {}
