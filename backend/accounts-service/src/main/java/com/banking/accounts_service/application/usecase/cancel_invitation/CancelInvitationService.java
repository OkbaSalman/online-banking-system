package com.banking.accounts_service.application.usecase.cancel_invitation;

import com.banking.accounts_service.application.port.AccountInvitationsRepositoryPort;
import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.cancel_invitation.dto.CancelInvitationCommand;
import com.banking.accounts_service.application.usecase.cancel_invitation.dto.CancelInvitationResult;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.domain.model.AccountInvitation;
import com.banking.accounts_service.domain.model.AccountInvitationStatus;
import com.banking.accounts_service.domain.model.MembershipRole;

public class CancelInvitationService implements CancelInvitationUseCase {

    private final AccountsRepositoryPort accounts;
    private final AccountInvitationsRepositoryPort invitations;

    public CancelInvitationService(AccountsRepositoryPort accounts, AccountInvitationsRepositoryPort invitations) {
        this.accounts = accounts;
        this.invitations = invitations;
    }

    @Override
    public CancelInvitationResult cancel(CancelInvitationCommand command) {
        validate(command);

        AccountInvitation invitation = invitations.findById(command.invitationId())
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        if (!command.requesterIsAdmin()) {
            var requesterMembership = accounts.findMembership(invitation.accountId(), command.requesterUserId())
                    .orElseThrow(() -> new ForbiddenException("Not a member of this account"));
            if (requesterMembership.role() != MembershipRole.OWNER) {
                throw new ForbiddenException("OWNER role required to cancel invitations");
            }
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
            return new CancelInvitationResult(expired);
        }

        if (invitation.status() != AccountInvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation is not pending");
        }

        var canceled = invitations.save(new AccountInvitation(
                invitation.id(),
                invitation.accountId(),
                invitation.invitedUserId(),
                invitation.invitedByUserId(),
                invitation.role(),
                AccountInvitationStatus.CANCELED,
                invitation.createdAtEpochMs(),
                invitation.expiresAtEpochMs(),
                now,
                    invitation.invitedByEmail()
        ));

        return new CancelInvitationResult(canceled);
    }

    private static void validate(CancelInvitationCommand command) {
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
