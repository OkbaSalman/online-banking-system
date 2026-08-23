package com.banking.ledger_service.adapter.out.jpa.repository;

import com.banking.ledger_service.adapter.out.jpa.entity.PostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostingJpaRepository extends JpaRepository<PostingEntity, UUID> {
    List<PostingEntity> findByEntryId(UUID entryId);
}