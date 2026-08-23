package com.banking.accounts_service.adapter.out.jpa.repository;

import com.banking.accounts_service.adapter.out.jpa.entity.AccountMembershipEntity;
import com.banking.accounts_service.adapter.out.jpa.entity.AccountMembershipEntity.Pk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountMembershipJpaRepository extends JpaRepository<AccountMembershipEntity, Pk> {

    Optional<AccountMembershipEntity> findByAccountIdAndUserId(UUID accountId, UUID userId);

    List<AccountMembershipEntity> findByAccountId(UUID accountId);

    void deleteByAccountIdAndUserId(UUID accountId, UUID userId);

    List<AccountMembershipEntity> findByUserId(UUID userId);
}
