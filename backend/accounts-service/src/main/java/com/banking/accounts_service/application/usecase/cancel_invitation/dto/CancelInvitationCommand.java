package com.banking.accounts_service.application.usecase.cancel_invitation.dto;

import java.util.UUID;

public record CancelInvitationCommand(
        UUID requesterUserId,
        boolean requesterIsAdmin,
        UUID invitationId
) {}
