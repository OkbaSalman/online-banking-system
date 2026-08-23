package com.banking.accounts_service.adapter.out.jpa;

import com.banking.accounts_service.adapter.out.jpa.entity.AccountEntity;
import com.banking.accounts_service.adapter.out.jpa.entity.AccountMembershipEntity;
import com.banking.accounts_service.adapter.out.jpa.repository.AccountJpaRepository;
import com.banking.accounts_service.adapter.out.jpa.repository.AccountMembershipJpaRepository;
import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.domain.model.Account;
import com.banking.accounts_service.domain.model.AccountType;
import com.banking.accounts_service.domain.model.AccountMembership;
import com.banking.accounts_service.domain.model.MembershipRole;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountsJpaAdapter implements AccountsRepositoryPort {

    private final AccountJpaRepository accounts;
    private final AccountMembershipJpaRepository memberships;

    public AccountsJpaAdapter(AccountJpaRepository accounts, AccountMembershipJpaRepository memberships) {
        this.accounts = accounts;
        this.memberships = memberships;
    }

    @Override
    public Optional<Account> findByCreatedByUserIdAndIdempotencyKey(UUID createdByUserId, String idempotencyKey) {
        return accounts.findByCreatedByUserIdAndIdempotencyKey(createdByUserId, idempotencyKey).map(this::toDomain);
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        return accounts.findById(accountId).map(this::toDomain);
    }

    @Override
    public List<Account> listAccountsByUserId(UUID userId) {
        List<UUID> accountIds = memberships.findByUserId(userId).stream().map(AccountMembershipEntity::getAccountId).toList();
        if (accountIds.isEmpty()) {
            return List.of();
        }
        return accounts.findByIdIn(accountIds).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Account> listAccountsByType(AccountType accountType, int limit, int offset) {
        int safeLimit = limit <= 0 ? 100 : Math.min(limit, 500);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<AccountEntity> pageItems = accounts.findByAccountType(accountType.name(), PageRequest.of(page, safeLimit + offsetInPage));
        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }
        return pageItems.subList(offsetInPage, pageItems.size()).stream().limit(safeLimit).map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public Account saveAccount(Account account) {
        return toDomain(accounts.save(toEntity(account)));
    }

    @Override
    public Optional<AccountMembership> findMembership(UUID accountId, UUID userId) {
        return memberships.findByAccountIdAndUserId(accountId, userId).map(this::toDomain);
    }

    @Override
    public List<AccountMembership> listMemberships(UUID accountId) {
        return memberships.findByAccountId(accountId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public AccountMembership upsertMembership(AccountMembership membership) {
        return toDomain(memberships.save(toEntity(membership)));
    }

    @Override
    @Transactional
    public void deleteMembership(UUID accountId, UUID userId) {
        memberships.deleteByAccountIdAndUserId(accountId, userId);
    }

    private Account toDomain(AccountEntity e) {
        return new Account(
                e.getId(),
                e.getIban(),
                e.getCreatedAtEpochMs(),
                AccountType.valueOf(e.getAccountType()),
                e.isFrozen(),
                e.getCreatedByUserId(),
                e.getIdempotencyKey(),
                e.getDisplayName()
        );
    }

    private AccountEntity toEntity(Account a) {
        return new AccountEntity(
                a.id(),
                a.iban(),
                a.createdAtEpochMs(),
                a.accountType().name(),
                a.frozen(),
                a.createdByUserId(),
                a.idempotencyKey(),
                a.displayName()
        );
    }

    private AccountMembership toDomain(AccountMembershipEntity e) {
        MembershipRole role = MembershipRole.valueOf(e.getRole());
        return new AccountMembership(e.getAccountId(), e.getUserId(), role, e.getCreatedAtEpochMs());
    }

    private AccountMembershipEntity toEntity(AccountMembership m) {
        return new AccountMembershipEntity(
                m.accountId(),
                m.userId(),
                m.role().name(),
                m.createdAtEpochMs()
        );
    }
}
