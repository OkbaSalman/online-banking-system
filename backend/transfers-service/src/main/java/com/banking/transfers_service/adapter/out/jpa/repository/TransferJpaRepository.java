package com.banking.transfers_service.adapter.out.jpa.repository;

import com.banking.transfers_service.adapter.out.jpa.entity.TransferEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferJpaRepository extends JpaRepository<TransferEntity, UUID> {

    Optional<TransferEntity> findByInitiatorUserIdAndIdempotencyKey(UUID initiatorUserId, String idempotencyKey);

    long countByInitiatorUserIdAndCreatedAtEpochMsGreaterThan(UUID initiatorUserId, long createdAtEpochMs);

    long countByFromAccountIdAndStatusAndCreatedAtEpochMsGreaterThan(UUID fromAccountId, String status, long createdAtEpochMs);

    @Query("""
            select t from TransferEntity t
            where t.initiatorUserId = :initiatorUserId
              and (:status is null or t.status = :status)
              and (:fromAccountId is null or t.fromAccountId = :fromAccountId)
              and (:toAccountId is null or t.toAccountId = :toAccountId)
            order by t.createdAtEpochMs desc
            """)
    List<TransferEntity> listMyTransfers(
            @Param("initiatorUserId") UUID initiatorUserId,
            @Param("status") String status,
            @Param("fromAccountId") UUID fromAccountId,
            @Param("toAccountId") UUID toAccountId,
            Pageable pageable
    );

    @Query("""
            select t from TransferEntity t
            where (
                    t.initiatorUserId = :userId
                    or t.fromAccountId in :accountIds
                    or t.toAccountId in :accountIds
            )
              and (:status is null or t.status = :status)
              and (:fromAccountId is null or t.fromAccountId = :fromAccountId)
              and (:toAccountId is null or t.toAccountId = :toAccountId)
            order by t.createdAtEpochMs desc
            """)
    List<TransferEntity> listVisibleToUser(
            @Param("userId") UUID userId,
            @Param("accountIds") List<UUID> accountIds,
            @Param("status") String status,
            @Param("fromAccountId") UUID fromAccountId,
            @Param("toAccountId") UUID toAccountId,
            Pageable pageable
    );

    @Query("""
            select t from TransferEntity t
            where (:initiatorUserId is null or t.initiatorUserId = :initiatorUserId)
              and (:status is null or t.status = :status)
              and (:fromAccountId is null or t.fromAccountId = :fromAccountId)
              and (:toAccountId is null or t.toAccountId = :toAccountId)
            order by t.createdAtEpochMs desc
            """)
    List<TransferEntity> adminListTransfers(
            @Param("initiatorUserId") UUID initiatorUserId,
            @Param("status") String status,
            @Param("fromAccountId") UUID fromAccountId,
            @Param("toAccountId") UUID toAccountId,
            Pageable pageable
    );

    @Query(value = """
            select
              extract(year from (to_timestamp(created_at_epoch_ms / 1000.0) at time zone 'UTC'))::int,
              extract(month from (to_timestamp(created_at_epoch_ms / 1000.0) at time zone 'UTC'))::int,
              coalesce(sum(fee_cents), 0),
              coalesce(sum(amount_cents), 0),
              count(*)
            from transfers
            where status = 'COMPLETED'
              and created_at_epoch_ms >= :fromMs
              and created_at_epoch_ms < :toMs
            group by 1, 2
            order by 1, 2
            """, nativeQuery = true)
    List<Object[]> aggregateCompletedRevenue(@Param("fromMs") long fromMs, @Param("toMs") long toMs);
}
