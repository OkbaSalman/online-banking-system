package com.banking.accounts_service.application.usecase.create_account;

import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.create_account.dto.CreateAccountCommand;
import com.banking.accounts_service.application.usecase.create_account.dto.CreateAccountResult;
import com.banking.accounts_service.domain.model.Account;
import com.banking.accounts_service.domain.model.AccountType;
import com.banking.accounts_service.domain.model.AccountMembership;
import com.banking.accounts_service.domain.model.MembershipRole;
import com.banking.accounts_service.domain.service.IbanGenerator;

import java.util.Optional;
import java.util.UUID;

public class CreateAccountService implements CreateAccountUseCase {

    private final AccountsRepositoryPort accounts;
    private final IbanGenerator ibanGenerator;

    public CreateAccountService(AccountsRepositoryPort accounts, IbanGenerator ibanGenerator) {
        this.accounts = accounts;
        this.ibanGenerator = ibanGenerator;
    }

    @Override
    public CreateAccountResult create(CreateAccountCommand command) {
        validate(command);

        AccountType accountType = command.accountType() == null ? AccountType.CHECKING : command.accountType();

        Optional<Account> existing = accounts.findByCreatedByUserIdAndIdempotencyKey(
                command.requesterUserId(),
                command.idempotencyKey()
        );
        if (existing.isPresent()) {
            return new CreateAccountResult(existing.get());
        }

        UUID accountId = UUID.randomUUID();
        long now = System.currentTimeMillis();

        String displayName = command.displayName() == null ? "" : command.displayName().trim();
        if (displayName.length() > 80) {
            displayName = displayName.substring(0, 80);
        }

        Account account = new Account(
                accountId,
                ibanGenerator.generate(),
                now,
                accountType,
                false,
                command.requesterUserId(),
                command.idempotencyKey(),
                displayName
        );

        Account saved = accounts.saveAccount(account);
        accounts.upsertMembership(new AccountMembership(saved.id(), command.requesterUserId(), MembershipRole.OWNER, now));

        return new CreateAccountResult(saved);
    }

    private static void validate(CreateAccountCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command is required");
        }
        if (command.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }
    }
}
