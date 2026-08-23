package com.banking.ledger_service.adapter.out.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ledger_postings")
public class PostingEntity {

    @Id
    private UUID id;

    private UUID entryId;

    private UUID accountId;

    private long amountCents;

    protected PostingEntity() {}

    public PostingEntity(UUID id, UUID entryId, UUID accountId, long amountCents) {
        this.id = id;
        this.entryId = entryId;
        this.accountId = accountId;
        this.amountCents = amountCents;
    }

    public UUID getId() { return id; }
    public UUID getEntryId() { return entryId; }
    public UUID getAccountId() { return accountId; }
    public long getAmountCents() { return amountCents; }
}