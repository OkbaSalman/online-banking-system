package com.banking.billing_service.adapter.out.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "billing_payments")
public class BillingPaymentEntity {

    @Id
    private UUID id;

    private UUID userId;

    private UUID fromAccountId;

    private UUID merchantAccountId;

    private long amountCents;

    private long createdAtEpochMs;

    private String status;

    private String idempotencyKey;

    private String description;

    private UUID transferId;

    private String failureMessage;

    private UUID subscriptionId;

    protected BillingPaymentEntity() {}

    public BillingPaymentEntity(
            UUID id,
            UUID userId,
            UUID fromAccountId,
            UUID merchantAccountId,
            long amountCents,
            long createdAtEpochMs,
            String status,
            String idempotencyKey,
            String description,
            UUID transferId,
            String failureMessage,
            UUID subscriptionId
    ) {
        this.id = id;
        this.userId = userId;
        this.fromAccountId = fromAccountId;
        this.merchantAccountId = merchantAccountId;
        this.amountCents = amountCents;
        this.createdAtEpochMs = createdAtEpochMs;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.description = description;
        this.transferId = transferId;
        this.failureMessage = failureMessage;
        this.subscriptionId = subscriptionId;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getMerchantAccountId() { return merchantAccountId; }
    public long getAmountCents() { return amountCents; }
    public long getCreatedAtEpochMs() { return createdAtEpochMs; }
    public String getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getDescription() { return description; }
    public UUID getTransferId() { return transferId; }
    public String getFailureMessage() { return failureMessage; }
    public UUID getSubscriptionId() { return subscriptionId; }
}
