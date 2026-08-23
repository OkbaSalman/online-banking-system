package com.banking.accounts_service.application.usecase.decline_invitation;

import com.banking.accounts_service.application.port.AccountInvitationsRepositoryPort;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.application.usecase.decline_invitation.dto.DeclineInvitationCommand;
import com.banking.accounts_service.application.usecase.decline_invitation.dto.DeclineInvitationResult;
import com.banking.accounts_service.domain.model.AccountInvitation;
import com.banking.accounts_service.domain.model.AccountInvitationStatus;

public class DeclineInvitationService implements DeclineInvitationUseCase {

    private final AccountInvitationsRepositoryPort invitations;

    public DeclineInvitationService(AccountInvitationsRepositoryPort invitations) {
        this.invitations = invitations;
    }

    @Override
    public DeclineInvitationResult decline(DeclineInvitationCommand command) {
        validate(command);

        AccountInvitation invitation = invitations.findById(command.invitationId())
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (!invitation.invitedUserId().equals(command.requesterUserId())) {
            throw new ForbiddenException("Not allowed to decline this invitation");
        }

        long now = System.currentTimeMillis();
        if (invitation.expiresAtEpochMs() <= now) {
            var expired = invitations.save(new AccountInvitation(
                    invitation.id(),
                    invitation.accountId(),
                    invitation.invitedUserId(),
                    invitation.invitedByUserId(),
                    invitation.role(),
                    AccountInvitationStatus.EXPIRED,
                    invitation.createdAtEpochMs(),
                    invitation.expiresAtEpochMs(),
                    now,
                    invitation.invitedByEmail()
            ));
            return new DeclineInvitationResult(expired);
        }

        if (invitation.status() != AccountInvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is not pending");
        }

        var declined = invitations.save(new AccountInvitation(
                invitation.id(),
                invitation.accountId(),
                invitation.invitedUserId(),
                invitation.invitedByUserId(),
                invitation.role(),
                AccountInvitationStatus.DECLINED,
                invitation.createdAtEpochMs(),
                invitation.expiresAtEpochMs(),
                now,
                    invitation.invitedByEmail()
        ));

        return new DeclineInvitationResult(declined);
    }

    private static void validate(DeclineInvitationCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command is required");
        }
        if (command.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
        if (command.invitationId() == null) {
            throw new IllegalArgumentException("invitationId is required");
        }
    }
}
