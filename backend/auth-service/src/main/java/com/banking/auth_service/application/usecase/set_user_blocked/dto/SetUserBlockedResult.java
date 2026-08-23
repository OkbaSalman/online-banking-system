package com.banking.auth_service.application.usecase.set_user_blocked.dto;

import java.util.UUID;

public record SetUserBlockedResult(
        UUID userId,
        boolean blocked
) {}
