package com.banking.accounts_service.application.usecase.set_account_frozen;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.common.exception.ForbiddenException;
import com.banking.accounts_service.application.usecase.common.exception.NotFoundException;
import com.banking.accounts_service.application.usecase.set_account_frozen.dto.SetAccountFrozenCommand;
import com.banking.accounts_service.application.usecase.set_account_frozen.dto.SetAccountFrozenResult;
import com.banking.accounts_service.domain.model.Account;

public class SetAccountFrozenService implements SetAccountFrozenUseCase {

    private final AccountsRepositoryPort accounts;

    public SetAccountFrozenService(AccountsRepositoryPort accounts) {
        this.accounts = accounts;
    }

    @Override
    public SetAccountFrozenResult setFrozen(SetAccountFrozenCommand command) {
        validate(command);

        if (!command.requesterIsAdmin()) {
            throw new ForbiddenException("ADMIN role required");
        }

        Account account = accounts.findById(command.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Account updated = new Account(
                account.id(),
                account.iban(),
                account.createdAtEpochMs(),
                account.accountType(),
                command.frozen(),
                account.createdByUserId(),
                account.idempotencyKey(),
                account.displayName()
        );

        accounts.saveAccount(updated);

        return new SetAccountFrozenResult(updated.id(), updated.frozen());
    }

    private static void validate(SetAccountFrozenCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command is required");
        }
        if (command.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
        if (command.accountId() == null) {
            throw new IllegalArgumentException("accountId is required");
        }
    }
}
