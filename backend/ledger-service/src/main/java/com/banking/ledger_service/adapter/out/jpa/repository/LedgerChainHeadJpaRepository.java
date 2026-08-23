package com.banking.ledger_service.adapter.out.jpa.repository;

import com.banking.ledger_service.adapter.out.jpa.entity.LedgerChainHeadEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface LedgerChainHeadJpaRepository extends JpaRepository<LedgerChainHeadEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from LedgerChainHeadEntity h where h.accountId = :accountId")
    Optional<LedgerChainHeadEntity> findForUpdate(UUID accountId);
}