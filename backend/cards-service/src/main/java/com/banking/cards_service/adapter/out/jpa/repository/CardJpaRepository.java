package com.banking.cards_service.adapter.out.jpa.repository;

import com.banking.cards_service.adapter.out.jpa.entity.CardEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardJpaRepository extends JpaRepository<CardEntity, UUID> {

    Optional<CardEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    @Query("""
            select c from CardEntity c
            where c.userId = :userId
            order by c.createdAtEpochMs desc
            """)
    List<CardEntity> listByUserId(@Param("userId") UUID userId, Pageable pageable);
}
