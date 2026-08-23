package com.banking.ledger_service.adapter.out.jpa.repository;

import com.banking.ledger_service.adapter.out.jpa.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, UUID> {

    Optional<LedgerEntryEntity> findByInitiatorUserIdAndIdempotencyKey(UUID initiatorUserId, String idempotencyKey);
}