package com.banking.accounts_service.application.usecase.set_account_display_name;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.application.usecase.set_account_display_name.dto.SetAccountDisplayNameCommand;
import com.banking.accounts_service.application.usecase.set_account_display_name.dto.SetAccountDisplayNameResult;
import com.banking.accounts_service.domain.model.Account;
import com.banking.accounts_service.domain.model.MembershipRole;

public class SetAccountDisplayNameService implements SetAccountDisplayNameUseCase {

    private final AccountsRepositoryPort accounts;

    public SetAccountDisplayNameService(AccountsRepositoryPort accounts) {
        this.accounts = accounts;
    }

    @Override
    public SetAccountDisplayNameResult setDisplayName(SetAccountDisplayNameCommand command) {
        if (command == null || command.requesterUserId() == null || command.accountId() == null) {
            throw new IllegalArgumentException("accountId and requesterUserId are required");
        }

        Account account = accounts.findById(command.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!command.requesterIsAdmin()) {
            var membership = accounts.findMembership(command.accountId(), command.requesterUserId())
                    .orElseThrow(() -> new ForbiddenException("Not a member of this account"));
            if (membership.role() != MembershipRole.OWNER) {
                throw new ForbiddenException("OWNER role required to rename this account");
            }
        }

        String displayName = command.displayName() == null ? "" : command.displayName().trim();
        if (displayName.length() > 80) {
            displayName = displayName.substring(0, 80);
        }

        Account updated = new Account(
                account.id(),
                account.iban(),
                account.createdAtEpochMs(),
                account.accountType(),
                account.frozen(),
                account.createdByUserId(),
                account.idempotencyKey(),
                displayName
        );

        return new SetAccountDisplayNameResult(accounts.saveAccount(updated));
    }
}
