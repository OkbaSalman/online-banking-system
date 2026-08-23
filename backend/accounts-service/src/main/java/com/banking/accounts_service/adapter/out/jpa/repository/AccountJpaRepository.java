package com.banking.accounts_service.adapter.out.jpa.repository;

import com.banking.accounts_service.adapter.out.jpa.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByCreatedByUserIdAndIdempotencyKey(UUID createdByUserId, String idempotencyKey);

    List<AccountEntity> findByIdIn(List<UUID> ids);

    List<AccountEntity> findByAccountType(String accountType, Pageable pageable);
}
