package com.banking.auth_service.adapter.out.jpa;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
 
import org.springframework.stereotype.Component;
 
import com.banking.auth_service.adapter.out.jpa.entity.EmailVerificationCodeEntity;
import com.banking.auth_service.adapter.out.jpa.repo.EmailVerificationCodeSpringDataRepository;
import com.banking.auth_service.application.port.EmailVerificationCodeRepositoryPort;
 
@Component
public class EmailVerificationCodeRepositoryAdapter implements EmailVerificationCodeRepositoryPort {
 
    private final EmailVerificationCodeSpringDataRepository repo;
 
    public EmailVerificationCodeRepositoryAdapter(EmailVerificationCodeSpringDataRepository repo) {
        this.repo = repo;
    }
 
    @Override
    public void createCode(UUID userId, String codeHash, Instant expiresAt) {
        EmailVerificationCodeEntity e = new EmailVerificationCodeEntity();
        e.id = UUID.randomUUID();
        e.userId = userId;
        e.codeHash = codeHash;
        e.expiresAt = OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC);
        e.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        repo.save(e);
    }

    @Override
    public Optional<EmailVerificationCodeRecord> findLatestActiveCode(UUID userId, Instant now) {
        OffsetDateTime nowUtc = OffsetDateTime.ofInstant(now, ZoneOffset.UTC);
 
        return repo
                .findFirstByUserIdAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(userId, nowUtc)
                .map(this::toRecord);
    }

    @Override
    public void consume(UUID codeId, Instant consumedAt) {
        EmailVerificationCodeEntity e = repo.findById(codeId)
                .orElseThrow(() -> new IllegalArgumentException("Verification code not found"));
 
        e.consumedAt = OffsetDateTime.ofInstant(consumedAt, ZoneOffset.UTC);
        repo.save(e);
    }

     private EmailVerificationCodeRecord toRecord(EmailVerificationCodeEntity e) {
        return new EmailVerificationCodeRecord(
                e.id,
                e.userId,
                e.codeHash,
                e.expiresAt.toInstant(),
                e.consumedAt == null ? null : e.consumedAt.toInstant(),
                e.createdAt.toInstant()
        );
    }
}
