package com.banking.accounts_service.application.usecase.decline_invitation.dto;

import java.util.UUID;

public record DeclineInvitationCommand(
        UUID requesterUserId,
        UUID invitationId
) {}
