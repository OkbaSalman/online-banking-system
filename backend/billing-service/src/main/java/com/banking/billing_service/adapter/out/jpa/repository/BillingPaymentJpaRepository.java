package com.banking.billing_service.adapter.out.jpa.repository;

import com.banking.billing_service.adapter.out.jpa.entity.BillingPaymentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingPaymentJpaRepository extends JpaRepository<BillingPaymentEntity, UUID> {

    Optional<BillingPaymentEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    @Query("""
            select p from BillingPaymentEntity p
            where p.userId = :userId
            order by p.createdAtEpochMs desc
            """)
    List<BillingPaymentEntity> listByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            select p from BillingPaymentEntity p
            where p.subscriptionId = :subscriptionId
            order by p.createdAtEpochMs desc
            """)
    List<BillingPaymentEntity> listBySubscriptionId(@Param("subscriptionId") UUID subscriptionId, Pageable pageable);
}
