package com.banking.auth_service.adapter.out.jpa;

import com.banking.auth_service.adapter.out.jpa.entity.PasswordResetTokenEntity;
import com.banking.auth_service.adapter.out.jpa.repo.PasswordResetTokenSpringDataRepository;
import com.banking.auth_service.application.port.PasswordResetTokenPort;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Component
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenPort {

    private final PasswordResetTokenSpringDataRepository repo;

    public PasswordResetTokenRepositoryAdapter(PasswordResetTokenSpringDataRepository repo) {
        this.repo = repo;
    }

    @Override
    public PasswordResetTokenRecord create(UUID userId, String tokenHash, Instant expiresAt) {
        PasswordResetTokenEntity e = new PasswordResetTokenEntity();
        e.id = UUID.randomUUID();
        e.userId = userId;
        e.tokenHash = tokenHash;
        e.expiresAt = OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC);
        e.consumedAt = null;
        e.createdAt = OffsetDateTime.now(ZoneOffset.UTC);

        return toRecord(repo.save(e));
    }

    @Override
    public Optional<PasswordResetTokenRecord> findActiveById(UUID id, Instant now) {
        OffsetDateTime nowUtc = OffsetDateTime.ofInstant(now, ZoneOffset.UTC);
        return repo.findActiveById(id, nowUtc).map(this::toRecord);
    }

    @Override
    public void consume(UUID id, Instant consumedAt) {
        PasswordResetTokenEntity e = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Reset token not found"));
        e.consumedAt = OffsetDateTime.ofInstant(consumedAt, ZoneOffset.UTC);
        repo.save(e);
    }

    private PasswordResetTokenRecord toRecord(PasswordResetTokenEntity e) {
        return new PasswordResetTokenRecord(
                e.id,
                e.userId,
                e.tokenHash,
                e.expiresAt.toInstant(),
                e.consumedAt == null ? null : e.consumedAt.toInstant(),
                e.createdAt.toInstant()
        );
    }
}
