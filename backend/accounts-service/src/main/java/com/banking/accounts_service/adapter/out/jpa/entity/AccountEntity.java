package com.banking.accounts_service.adapter.out.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "iban", nullable = false, unique = true, length = 34)
    private String iban;

    @Column(name = "created_at_epoch_ms", nullable = false)
    private long createdAtEpochMs;

    @Column(name = "account_type", nullable = false, length = 32)
    private String accountType;

    @Column(name = "frozen", nullable = false)
    private boolean frozen;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "display_name", nullable = false)
    private String displayName = "";

    protected AccountEntity() {
    }

    public AccountEntity(
            UUID id,
            String iban,
            long createdAtEpochMs,
            String accountType,
            boolean frozen,
            UUID createdByUserId,
            String idempotencyKey,
            String displayName
    ) {
        this.id = id;
        this.iban = iban;
        this.createdAtEpochMs = createdAtEpochMs;
        this.accountType = accountType;
        this.frozen = frozen;
        this.createdByUserId = createdByUserId;
        this.idempotencyKey = idempotencyKey;
        this.displayName = displayName == null ? "" : displayName;
    }

    public UUID getId() {
        return id;
    }

    public String getIban() {
        return iban;
    }

    public long getCreatedAtEpochMs() {
        return createdAtEpochMs;
    }

    public String getAccountType() {
        return accountType;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getDisplayName() {
        return displayName == null ? "" : displayName;
    }
}
