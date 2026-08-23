package com.banking.ledger_service.adapter.out.jpa.repository;

import com.banking.ledger_service.adapter.out.jpa.entity.AccountBalanceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountBalanceJpaRepository extends JpaRepository<AccountBalanceEntity, UUID> {

    Optional<AccountBalanceEntity> findByAccountId(UUID accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from AccountBalanceEntity b where b.accountId in :accountIds")
    List<AccountBalanceEntity> findAllForUpdate(List<UUID> accountIds);
}