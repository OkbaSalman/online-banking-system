package com.banking.billing_service.adapter.out.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity {

    @Id
    private UUID id;

    private UUID userId;

    private UUID fromAccountId;

    private UUID merchantAccountId;

    private long amountCents;

    private String intervalUnit;

    private int intervalCount;

    private long nextChargeAtEpochMs;

    private String status;

    private long createdAtEpochMs;

    private String idempotencyKey;

    private String description;

    private Long lastAttemptAtEpochMs;

    private int consecutiveFailures;

    private long dueAnchorEpochMs;

    protected SubscriptionEntity() {}

    public SubscriptionEntity(
            UUID id,
            UUID userId,
            UUID fromAccountId,
            UUID merchantAccountId,
            long amountCents,
            String intervalUnit,
            int intervalCount,
            long nextChargeAtEpochMs,
            String status,
            long createdAtEpochMs,
            String idempotencyKey,
            String description,
            Long lastAttemptAtEpochMs,
            int consecutiveFailures,
            long dueAnchorEpochMs
    ) {
        this.id = id;
        this.userId = userId;
        this.fromAccountId = fromAccountId;
        this.merchantAccountId = merchantAccountId;
        this.amountCents = amountCents;
        this.intervalUnit = intervalUnit;
        this.intervalCount = intervalCount;
        this.nextChargeAtEpochMs = nextChargeAtEpochMs;
        this.status = status;
        this.createdAtEpochMs = createdAtEpochMs;
        this.idempotencyKey = idempotencyKey;
        this.description = description;
        this.lastAttemptAtEpochMs = lastAttemptAtEpochMs;
        this.consecutiveFailures = consecutiveFailures;
        this.dueAnchorEpochMs = dueAnchorEpochMs;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getMerchantAccountId() { return merchantAccountId; }
    public long getAmountCents() { return amountCents; }
    public String getIntervalUnit() { return intervalUnit; }
    public int getIntervalCount() { return intervalCount; }
    public long getNextChargeAtEpochMs() { return nextChargeAtEpochMs; }
    public String getStatus() { return status; }
    public long getCreatedAtEpochMs() { return createdAtEpochMs; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getDescription() { return description; }
    public Long getLastAttemptAtEpochMs() { return lastAttemptAtEpochMs; }
    public int getConsecutiveFailures() { return consecutiveFailures; }
    public long getDueAnchorEpochMs() { return dueAnchorEpochMs; }
}
