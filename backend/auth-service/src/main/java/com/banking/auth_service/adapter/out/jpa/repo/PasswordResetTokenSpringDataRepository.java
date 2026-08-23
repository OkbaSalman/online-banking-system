package com.banking.auth_service.adapter.out.jpa.repo;

import com.banking.auth_service.adapter.out.jpa.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenSpringDataRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    @Query("""
            select t from PasswordResetTokenEntity t
            where t.id = :id
              and t.consumedAt is null
              and t.expiresAt > :now
            """)
    Optional<PasswordResetTokenEntity> findActiveById(@Param("id") UUID id, @Param("now") OffsetDateTime now);
}
