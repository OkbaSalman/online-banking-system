package com.banking.accounts_service.adapter.out.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "account_memberships")
@IdClass(AccountMembershipEntity.Pk.class)
public class AccountMembershipEntity {

    @Id
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role", nullable = false, length = 32)
    private String role;

    @Column(name = "created_at_epoch_ms", nullable = false)
    private long createdAtEpochMs;

    protected AccountMembershipEntity() {
    }

    public AccountMembershipEntity(UUID accountId, UUID userId, String role, long createdAtEpochMs) {
        this.accountId = accountId;
        this.userId = userId;
        this.role = role;
        this.createdAtEpochMs = createdAtEpochMs;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public long getCreatedAtEpochMs() {
        return createdAtEpochMs;
    }

    public static class Pk implements Serializable {
        private UUID accountId;
        private UUID userId;

        public Pk() {
        }

        public Pk(UUID accountId, UUID userId) {
            this.accountId = accountId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pk pk = (Pk) o;
            return Objects.equals(accountId, pk.accountId) && Objects.equals(userId, pk.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, userId);
        }
    }
}
