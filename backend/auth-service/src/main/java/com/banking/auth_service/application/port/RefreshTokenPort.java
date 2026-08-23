package com.banking.auth_service.application.port;
 
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
 
public interface RefreshTokenPort {
 
    record RefreshTokenRecord(
            UUID id,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            Instant revokedAt,
            Instant createdAt
    ) {}
 
    void revokeAllActiveForUser(UUID userId, Instant revokedAt);
 
    RefreshTokenRecord create(UUID userId, String tokenHash, Instant expiresAt);
 
    Optional<RefreshTokenRecord> findActiveById(UUID id, Instant now);
 
    void revokeById(UUID id, Instant revokedAt);
}