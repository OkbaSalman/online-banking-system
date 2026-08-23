package com.banking.ledger_service.adapter.out.jpa.repository;

import com.banking.ledger_service.adapter.out.jpa.entity.AccountLedgerItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountLedgerItemJpaRepository extends JpaRepository<AccountLedgerItemEntity, UUID> {

    List<AccountLedgerItemEntity> findByAccountIdOrderByCreatedAtEpochMsDesc(UUID accountId, Pageable pageable);

    List<AccountLedgerItemEntity> findByAccountIdOrderBySeqAsc(UUID accountId);
}
