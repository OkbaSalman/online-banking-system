package com.banking.billing_service.adapter.out.jpa.repository;

import com.banking.billing_service.adapter.out.jpa.entity.SubscriptionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, UUID> {

    Optional<SubscriptionEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    @Query("""
            select s from SubscriptionEntity s
            where s.userId = :userId
            order by s.createdAtEpochMs desc
            """)
    List<SubscriptionEntity> listByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            select s from SubscriptionEntity s
            where s.status = 'ACTIVE'
              and s.nextChargeAtEpochMs <= :nowEpochMs
            order by s.nextChargeAtEpochMs asc
            """)
    List<SubscriptionEntity> listDueActive(@Param("nowEpochMs") long nowEpochMs, Pageable pageable);
}
