package com.banking.cards_service.adapter.out.jpa.repository;

import com.banking.cards_service.adapter.out.jpa.entity.CardChargeEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardChargeJpaRepository extends JpaRepository<CardChargeEntity, UUID> {

    Optional<CardChargeEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    @Query("""
            select c from CardChargeEntity c
            where c.userId = :userId
            order by c.createdAtEpochMs desc
            """)
    List<CardChargeEntity> listByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            select c from CardChargeEntity c
            where c.cardId = :cardId
            order by c.createdAtEpochMs desc
            """)
    List<CardChargeEntity> listByCardId(@Param("cardId") UUID cardId, Pageable pageable);

    @Query("""
            select coalesce(sum(c.amountCents), 0)
            from CardChargeEntity c
            where c.cardId = :cardId
              and c.status in :statuses
              and c.createdAtEpochMs >= :sinceEpochMs
            """)
    long sumAmountSince(
            @Param("cardId") UUID cardId,
            @Param("statuses") java.util.Collection<String> statuses,
            @Param("sinceEpochMs") long sinceEpochMs
    );
}
