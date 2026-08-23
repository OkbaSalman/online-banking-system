package com.banking.accounts_service.application.usecase.remove_member;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.application.usecase.remove_member.dto.RemoveMemberCommand;
import com.banking.accounts_service.application.usecase.remove_member.dto.RemoveMemberResult;
import com.banking.accounts_service.domain.model.MembershipRole;

public class RemoveMemberService implements RemoveMemberUseCase {

    private final AccountsRepositoryPort accounts;

    public RemoveMemberService(AccountsRepositoryPort accounts) {
        this.accounts = accounts;
    }

    @Override
    public RemoveMemberResult remove(RemoveMemberCommand command) {
        validate(command);

        accounts.findById(command.accountId()).orElseThrow(() -> new NotFoundException("Account not found"));

        if (!command.requesterIsAdmin()) {
            var requesterMembership = accounts.findMembership(command.accountId(), command.requesterUserId())
                    .orElseThrow(() -> new ForbiddenException("Not a member of this account"));
            if (requesterMembership.role() != MembershipRole.OWNER) {
                throw new ForbiddenException("OWNER role required to remove members");
            }
        }

        if (command.userIdToRemove().equals(command.requesterUserId()) && !command.requesterIsAdmin()) {
            throw new IllegalArgumentException("Cannot remove yourself");
        }

        var targetMembership = accounts.findMembership(command.accountId(), command.userIdToRemove())
                .orElseThrow(() -> new NotFoundException("Membership not found"));

        if (!command.requesterIsAdmin() && targetMembership.role() == MembershipRole.OWNER) {
            throw new ForbiddenException("Cannot remove OWNER");
        }

        accounts.deleteMembership(command.accountId(), command.userIdToRemove());
        return new RemoveMemberResult(true);
    }

    private static void validate(RemoveMemberCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command is required");
        }
        if (command.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
        if (command.accountId() == null) {
            throw new IllegalArgumentException("accountId is required");
        }
        if (command.userIdToRemove() == null) {
            throw new IllegalArgumentException("userIdToRemove is required");
        }
    }
}
