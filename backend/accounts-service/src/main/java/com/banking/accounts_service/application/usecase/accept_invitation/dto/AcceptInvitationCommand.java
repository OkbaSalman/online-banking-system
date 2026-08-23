package com.banking.accounts_service.application.usecase.accept_invitation.dto;

import java.util.UUID;

public record AcceptInvitationCommand(
        UUID requesterUserId,
        UUID invitationId
) {}
