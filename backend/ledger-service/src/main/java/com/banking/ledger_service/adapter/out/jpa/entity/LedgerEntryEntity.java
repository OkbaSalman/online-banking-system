package com.banking.ledger_service.adapter.out.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryEntity {

    @Id
    private UUID id;

    private UUID initiatorUserId;

    private String idempotencyKey;

    private String type;

    private String description;

    private long createdAtEpochMs;

    private UUID fromAccountId;

    private UUID toAccountId;

    private long amountCents;

    protected LedgerEntryEntity() {}

    public LedgerEntryEntity(
            UUID id,
            UUID initiatorUserId,
            String idempotencyKey,
            String type,
            String description,
            long createdAtEpochMs,
            UUID fromAccountId,
            UUID toAccountId,
            long amountCents
    ) {
        this.id = id;
        this.initiatorUserId = initiatorUserId;
        this.idempotencyKey = idempotencyKey;
        this.type = type;
        this.description = description;
        this.createdAtEpochMs = createdAtEpochMs;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amountCents = amountCents;
    }

    public UUID getId() { return id; }
    public UUID getInitiatorUserId() { return initiatorUserId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public long getCreatedAtEpochMs() { return createdAtEpochMs; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getToAccountId() { return toAccountId; }
    public long getAmountCents() { return amountCents; }
}