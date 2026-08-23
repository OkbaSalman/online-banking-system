package com.banking.cards_service.adapter.out.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "cards")
public class CardEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "funding_account_id", nullable = false)
    private UUID fundingAccountId;

    @Column(name = "last4", nullable = false)
    private String last4;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at_epoch_ms", nullable = false)
    private long createdAtEpochMs;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "daily_limit_cents", nullable = false)
    private long dailyLimitCents;

    @Column(name = "monthly_limit_cents", nullable = false)
    private long monthlyLimitCents;

    @Column(name = "per_transaction_limit_cents", nullable = false)
    private long perTransactionLimitCents;

    protected CardEntity() {}

    public CardEntity(
            UUID id,
            UUID userId,
            UUID fundingAccountId,
            String last4,
            String status,
            long createdAtEpochMs,
            String idempotencyKey,
            String nickname,
            long dailyLimitCents,
            long monthlyLimitCents,
            long perTransactionLimitCents
    ) {
        this.id = id;
        this.userId = userId;
        this.fundingAccountId = fundingAccountId;
        this.last4 = last4;
        this.status = status;
        this.createdAtEpochMs = createdAtEpochMs;
        this.idempotencyKey = idempotencyKey;
        this.nickname = nickname;
        this.dailyLimitCents = dailyLimitCents;
        this.monthlyLimitCents = monthlyLimitCents;
        this.perTransactionLimitCents = perTransactionLimitCents;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getFundingAccountId() { return fundingAccountId; }
    public String getLast4() { return last4; }
    public String getStatus() { return status; }
    public long getCreatedAtEpochMs() { return createdAtEpochMs; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getNickname() { return nickname; }
    public long getDailyLimitCents() { return dailyLimitCents; }
    public long getMonthlyLimitCents() { return monthlyLimitCents; }
    public long getPerTransactionLimitCents() { return perTransactionLimitCents; }
}
