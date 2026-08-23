package com.banking.accounts_service.application.port;

import com.banking.accounts_service.domain.model.Account;
import com.banking.accounts_service.domain.model.AccountType;
import com.banking.accounts_service.domain.model.AccountMembership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountsRepositoryPort {

    Optional<Account> findByCreatedByUserIdAndIdempotencyKey(UUID createdByUserId, String idempotencyKey);

    Optional<Account> findById(UUID accountId);

    List<Account> listAccountsByUserId(UUID userId);

    List<Account> listAccountsByType(AccountType accountType, int limit, int offset);

    Account saveAccount(Account account);

    Optional<AccountMembership> findMembership(UUID accountId, UUID userId);

    List<AccountMembership> listMemberships(UUID accountId);

    AccountMembership upsertMembership(AccountMembership membership);

    void deleteMembership(UUID accountId, UUID userId);
}
