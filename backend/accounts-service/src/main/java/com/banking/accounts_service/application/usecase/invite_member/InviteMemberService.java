package com.banking.accounts_service.application.usecase.invite_member;

import com.banking.accounts_service.application.port.AccountInvitationNotificationPort;
import com.banking.accounts_service.application.port.AccountInvitationsRepositoryPort;
import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.application.usecase.invite_member.dto.InviteMemberCommand;
import com.banking.accounts_service.application.usecase.invite_member.dto.InviteMemberResult;
import com.banking.accounts_service.domain.model.AccountInvitation;
import com.banking.accounts_service.domain.model.AccountInvitationStatus;
import com.banking.accounts_service.domain.model.MembershipRole;

import java.util.UUID;

public class InviteMemberService implements InviteMemberUseCase {

    private static final long DEFAULT_TTL_SECONDS = 7L * 24 * 60 * 60;

    private final AccountsRepositoryPort accounts;
    private final AccountInvitationsRepositoryPort invitations;
    private final AccountInvitationNotificationPort notifications;

    public InviteMemberService(
            AccountsRepositoryPort accounts,
            AccountInvitationsRepositoryPort invitations,
            AccountInvitationNotificationPort notifications
    ) {
        this.accounts = accounts;
        this.invitations = invitations;
        this.notifications = notifications;
    }

    @Override
    public InviteMemberResult invite(InviteMemberCommand command) {
        validate(command);

        var account = accounts.findById(command.accountId()).orElseThrow(() -> new NotFoundException("Account not found"));

        if (!command.requesterIsAdmin()) {
            var requesterMembership = accounts.findMembership(command.accountId(), command.requesterUserId())
                    .orElseThrow(() -> new ForbiddenException("Not a member of this account"));
            if (requesterMembership.role() != MembershipRole.OWNER) {
                throw new ForbiddenException("OWNER role required to invite members");
            }
        }

        accounts.findMembership(command.accountId(), command.invitedUserId()).ifPresent(m -> {
            throw new IllegalArgumentException("User is already a member of this account");
        });

        long now = System.currentTimeMillis();
        invitations.findPendingByAccountIdAndInvitedUserId(command.accountId(), command.invitedUserId()).ifPresent(existing -> {
            if (existing.expiresAtEpochMs() > now) {
                throw new IllegalArgumentException("A pending invitation already exists for this user");
            }
        });

        long ttlSeconds = command.ttlSeconds() == null || command.ttlSeconds() <= 0
                ? DEFAULT_TTL_SECONDS
                : command.ttlSeconds();

        MembershipRole role = command.role() == null ? MembershipRole.MEMBER : command.role();

        AccountInvitation invitation = new AccountInvitation(
                UUID.randomUUID(),
                command.accountId(),
                command.invitedUserId(),
                command.requesterUserId(),
                role,
                AccountInvitationStatus.PENDING,
                now,
                now + ttlSeconds * 1000,
                null,
                command.invitedByEmail()
        );

        var saved = invitations.save(invitation);
        notifications.sendInvitationRequested(saved, account);
        return new InviteMemberResult(saved);
    }

    private static void validate(InviteMemberCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command is required");
        }
        if (command.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
        if (command.accountId() == null) {
            throw new IllegalArgumentException("accountId is required");
        }
        if (command.invitedUserId() == null) {
            throw new IllegalArgumentException("invitedUserId is required");
        }
        if (command.invitedUserId().equals(command.requesterUserId())) {
            throw new IllegalArgumentException("Cannot invite yourself");
        }
    }
}
