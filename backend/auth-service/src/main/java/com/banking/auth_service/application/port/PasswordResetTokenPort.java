package com.banking.auth_service.application.port;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenPort {

    record PasswordResetTokenRecord(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant consumedAt,
            Instant createdAt
    ) {}

    PasswordResetTokenRecord create(UUID userId, String tokenHash, Instant expiresAt);

    Optional<PasswordResetTokenRecord> findActiveById(UUID id, Instant now);

    void consume(UUID id, Instant consumedAt);
}
