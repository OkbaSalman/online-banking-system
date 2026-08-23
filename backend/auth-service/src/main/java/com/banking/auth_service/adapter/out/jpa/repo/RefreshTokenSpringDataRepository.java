package com.banking.auth_service.adapter.out.jpa.repo;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banking.auth_service.adapter.out.jpa.entity.RefreshTokenEntity;


public interface RefreshTokenSpringDataRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    @Query("""
            select rt
            from RefreshTokenEntity rt
            where rt.id = :id
              and rt.revokedAt is null
              and rt.expiresAt > :now
            """)
    Optional<RefreshTokenEntity> findActiveById(@Param("id") UUID id, @Param("now") OffsetDateTime now);

    /**
     * Revoke every non-revoked token for the user, including expired ones.
     * The partial unique index refresh_tokens_one_active_per_user keys on
     * (user_id) WHERE revoked_at IS NULL — expired-but-unrevoked rows still
     * block inserting a new refresh token on login.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshTokenEntity rt
            set rt.revokedAt = :revokedAt
            where rt.userId = :userId
              and rt.revokedAt is null
            """)
    int revokeAllActiveForUser(
            @Param("userId") UUID userId,
            @Param("revokedAt") OffsetDateTime revokedAt,
            @Param("now") OffsetDateTime now
    );

    @Modifying
    @Query("""
        update RefreshTokenEntity rt
        set rt.revokedAt = :revokedAt
        where rt.id = :id
          and rt.revokedAt is null
        """)
    int revokeById(@Param("id") UUID id, @Param("revokedAt") OffsetDateTime revokedAt);
}