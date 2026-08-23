package com.banking.transfers_service.adapter.out.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "transfers")
public class TransferEntity {

    @Id
    private UUID id;

    private UUID initiatorUserId;

    private UUID fromAccountId;

    private UUID toAccountId;

    private long amountCents;

    private long feeCents;

    private String idempotencyKey;

    private String description;

    private long createdAtEpochMs;

    private String status;

    private UUID ledgerEntryId;

    private UUID feeLedgerEntryId;

    private String failureMessage;

    protected TransferEntity() {}

    public TransferEntity(
            UUID id,
            UUID initiatorUserId,
            UUID fromAccountId,
            UUID toAccountId,
            long amountCents,
            long feeCents,
            String idempotencyKey,
            String description,
            long createdAtEpochMs,
            String status,
            UUID ledgerEntryId,
            UUID feeLedgerEntryId,
            String failureMessage
    ) {
        this.id = id;
        this.initiatorUserId = initiatorUserId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amountCents = amountCents;
        this.feeCents = feeCents;
        this.idempotencyKey = idempotencyKey;
        this.description = description;
        this.createdAtEpochMs = createdAtEpochMs;
        this.status = status;
        this.ledgerEntryId = ledgerEntryId;
        this.feeLedgerEntryId = feeLedgerEntryId;
        this.failureMessage = failureMessage;
    }

    public UUID getId() { return id; }
    public UUID getInitiatorUserId() { return initiatorUserId; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getToAccountId() { return toAccountId; }
    public long getAmountCents() { return amountCents; }
    public long getFeeCents() { return feeCents; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getDescription() { return description; }
    public long getCreatedAtEpochMs() { return createdAtEpochMs; }
    public String getStatus() { return status; }
    public UUID getLedgerEntryId() { return ledgerEntryId; }
    public UUID getFeeLedgerEntryId() { return feeLedgerEntryId; }
    public String getFailureMessage() { return failureMessage; }
}
