package com.banking.auth_service.adapter.out.jpa;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.banking.auth_service.adapter.out.jpa.entity.RefreshTokenEntity;
import com.banking.auth_service.adapter.out.jpa.repo.RefreshTokenSpringDataRepository;
import com.banking.auth_service.application.port.RefreshTokenPort;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenPort {

    private final RefreshTokenSpringDataRepository repo;

    public RefreshTokenRepositoryAdapter(RefreshTokenSpringDataRepository repo) {
        this.repo = repo;
    }

    @Transactional
    @Override
    public void revokeAllActiveForUser(UUID userId, Instant revokedAt) {
        OffsetDateTime revokedAtUtc = OffsetDateTime.ofInstant(revokedAt, ZoneOffset.UTC);
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        repo.revokeAllActiveForUser(userId, revokedAtUtc, nowUtc);
    }

    @Override
    public RefreshTokenRecord create(UUID userId, String tokenHash, Instant expiresAt) {
        RefreshTokenEntity e = new RefreshTokenEntity();
        e.id = UUID.randomUUID();
        e.userId = userId;
        e.tokenHash = tokenHash;
        e.expiresAt = OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC);
        e.revokedAt = null;
        e.createdAt = OffsetDateTime.now(ZoneOffset.UTC);

        RefreshTokenEntity saved = repo.save(e);
        return toRecord(saved);
    }

    @Override
    public Optional<RefreshTokenRecord> findActiveById(UUID id, Instant now) {
        OffsetDateTime nowUtc = OffsetDateTime.ofInstant(now, ZoneOffset.UTC);
        return repo.findActiveById(id, nowUtc).map(this::toRecord);
    }

    @Override
    public void revokeById(UUID id, Instant revokedAt) {
        OffsetDateTime revokedAtUtc = OffsetDateTime.ofInstant(revokedAt, ZoneOffset.UTC);
        int updated = repo.revokeById(id, revokedAtUtc);

        if (updated == 0) {
            throw new IllegalArgumentException("Refresh token not found");
        }
    }

    private RefreshTokenRecord toRecord(RefreshTokenEntity e) {
        return new RefreshTokenRecord(
                e.id,
                e.userId,
                e.tokenHash,
                e.expiresAt.toInstant(),
                e.revokedAt == null ? null : e.revokedAt.toInstant(),
                e.createdAt.toInstant()
        );
    }
}