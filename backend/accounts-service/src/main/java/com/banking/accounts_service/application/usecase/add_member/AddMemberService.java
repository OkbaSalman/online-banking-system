package com.banking.accounts_service.application.usecase.add_member;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.add_member.dto.AddMemberCommand;
import com.banking.accounts_service.application.usecase.add_member.dto.AddMemberResult;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.domain.model.AccountMembership;
import com.banking.accounts_service.domain.model.MembershipRole;

public class AddMemberService implements AddMemberUseCase {

    private final AccountsRepositoryPort accounts;

    public AddMemberService(AccountsRepositoryPort accounts) {
        this.accounts = accounts;
    }

    @Override
    public AddMemberResult add(AddMemberCommand command) {
        validate(command);

        accounts.findById(command.accountId()).orElseThrow(() -> new NotFoundException("Account not found"));

        if (!command.requesterIsAdmin()) {
            throw new ForbiddenException("Direct member add is disabled. Use invitation flow (invite + accept).");
        }

        long now = System.currentTimeMillis();
        MembershipRole role = command.role() == null ? MembershipRole.MEMBER : command.role();
        var saved = accounts.upsertMembership(new AccountMembership(command.accountId(), command.memberUserId(), role, now));
        return new AddMemberResult(saved);
    }

    private static void validate(AddMemberCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command is required");
        }
        if (command.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
        if (command.accountId() == null) {
            throw new IllegalArgumentException("accountId is required");
        }
        if (command.memberUserId() == null) {
            throw new IllegalArgumentException("memberUserId is required");
        }
    }
}
