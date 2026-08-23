package com.banking.auth_service.adapter.out.jpa.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_verification_codes")
public class EmailVerificationCodeEntity {
 
    @Id
    public UUID id;
 
    @Column(name = "user_id", nullable = false)
    public UUID userId;
 
    @Column(name = "code_hash", nullable = false)
    public String codeHash;
 
    @Column(name = "expires_at", nullable = false)
    public OffsetDateTime expiresAt;
 
    @Column(name = "consumed_at")
    public OffsetDateTime consumedAt;
 
    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;
}
