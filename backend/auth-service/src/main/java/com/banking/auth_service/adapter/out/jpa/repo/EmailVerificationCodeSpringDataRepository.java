package com.banking.auth_service.adapter.out.jpa.repo;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
 
import org.springframework.data.jpa.repository.JpaRepository;
 
import com.banking.auth_service.adapter.out.jpa.entity.EmailVerificationCodeEntity;

public interface EmailVerificationCodeSpringDataRepository extends JpaRepository<EmailVerificationCodeEntity, UUID> {
    Optional<EmailVerificationCodeEntity> findFirstByUserIdAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            UUID userId,
            OffsetDateTime now
    );
}
