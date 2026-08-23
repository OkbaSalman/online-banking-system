package com.banking.cards_service.adapter.out.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "card_charges")
public class CardChargeEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "card_id", nullable = false)
    private UUID cardId;

    @Column(name = "merchant_account_id", nullable = false)
    private UUID merchantAccountId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "created_at_epoch_ms", nullable = false)
    private long createdAtEpochMs;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "description")
    private String description;

    @Column(name = "transfer_id")
    private UUID transferId;

    @Column(name = "failure_message")
    private String failureMessage;

    @Column(name = "fee_cents", nullable = false)
    private long feeCents;

    protected CardChargeEntity() {}

    public CardChargeEntity(
            UUID id,
            UUID userId,
            UUID cardId,
            UUID merchantAccountId,
            long amountCents,
            long createdAtEpochMs,
            String status,
            String idempotencyKey,
            String description,
            UUID transferId,
            String failureMessage,
            long feeCents
    ) {
        this.id = id;
        this.userId = userId;
        this.cardId = cardId;
        this.merchantAccountId = merchantAccountId;
        this.amountCents = amountCents;
        this.createdAtEpochMs = createdAtEpochMs;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.description = description;
        this.transferId = transferId;
        this.failureMessage = failureMessage;
        this.feeCents = feeCents;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCardId() { return cardId; }
    public UUID getMerchantAccountId() { return merchantAccountId; }
    public long getAmountCents() { return amountCents; }
    public long getCreatedAtEpochMs() { return createdAtEpochMs; }
    public String getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getDescription() { return description; }
    public UUID getTransferId() { return transferId; }
    public String getFailureMessage() { return failureMessage; }
    public long getFeeCents() { return feeCents; }
}
