package com.banking.auth_service.application.port;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationCodeRepositoryPort {
    record EmailVerificationCodeRecord(
            UUID id,
            UUID userId,
            String codeHash,
            Instant expiresAt,
            Instant consumedAt,
            Instant createdAt
    ) {}

    void createCode(UUID userId, String codeHash, Instant expiresAt);

    Optional<EmailVerificationCodeRecord> findLatestActiveCode(UUID userId, Instant now);
 
    void consume(UUID codeId, Instant consumedAt);
}
