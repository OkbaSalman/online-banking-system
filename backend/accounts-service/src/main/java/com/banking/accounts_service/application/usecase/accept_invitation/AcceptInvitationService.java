package com.banking.accounts_service.application.usecase.accept_invitation;

import com.banking.accounts_service.application.port.AccountInvitationsRepositoryPort;
import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.accept_invitation.dto.AcceptInvitationCommand;
import com.banking.accounts_service.application.usecase.accept_invitation.dto.AcceptInvitationResult;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.domain.model.AccountInvitation;
import com.banking.accounts_service.domain.model.AccountInvitationStatus;
import com.banking.accounts_service.domain.model.AccountMembership;

public class AcceptInvitationService implements AcceptInvitationUseCase {

    private final AccountsRepositoryPort accounts;
    private final AccountInvitationsRepositoryPort invitations;

    public AcceptInvitationService(AccountsRepositoryPort accounts, AccountInvitationsRepositoryPort invitations) {
        this.accounts = accounts;
        this.invitations = invitations;
    }

    @Override
    public AcceptInvitationResult accept(AcceptInvitationCommand command) {
        validate(command);

        AccountInvitation invitation = invitations.findById(command.invitationId())
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (!invitation.invitedUserId().equals(command.requesterUserId())) {
            throw new ForbiddenException("Not allowed to accept this invitation");
        }

        long now = System.currentTimeMillis();
        if (invitation.expiresAtEpochMs() <= now) {
            invitations.save(new AccountInvitation(
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
            throw new IllegalArgumentException("Invitation has expired");
        }

        if (invitation.status() != AccountInvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is not pending");
        }

        accounts.findById(invitation.accountId()).orElseThrow(() -> new NotFoundException("Account not found"));

        var updated = invitations.save(new AccountInvitation(
                invitation.id(),
                invitation.accountId(),
                invitation.invitedUserId(),
                invitation.invitedByUserId(),
                invitation.role(),
                AccountInvitationStatus.ACCEPTED,
                invitation.createdAtEpochMs(),
                invitation.expiresAtEpochMs(),
                now,
                invitation.invitedByEmail()
        ));

        var membership = accounts.upsertMembership(new AccountMembership(
                updated.accountId(),
                updated.invitedUserId(),
                updated.role(),
                now
        ));

        return new AcceptInvitationResult(membership);
    }

    private static void validate(AcceptInvitationCommand command) {
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
