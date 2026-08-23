package com.banking.ledger_service.adapter.out.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "account_ledger_items")
public class AccountLedgerItemEntity {

    @Id
    private UUID id;

    private UUID accountId;

    private UUID entryId;

    private long createdAtEpochMs;

    private long amountCents;

    private UUID counterpartyAccountId;

    private long seq;

    private String prevHash;

    private String itemHash;

    protected AccountLedgerItemEntity() {}

    public AccountLedgerItemEntity(
            UUID id,
            UUID accountId,
            UUID entryId,
            long createdAtEpochMs,
            long amountCents,
            UUID counterpartyAccountId,
            long seq,
            String prevHash,
            String itemHash
    ) {
        this.id = id;
        this.accountId = accountId;
        this.entryId = entryId;
        this.createdAtEpochMs = createdAtEpochMs;
        this.amountCents = amountCents;
        this.counterpartyAccountId = counterpartyAccountId;
        this.seq = seq;
        this.prevHash = prevHash;
        this.itemHash = itemHash;
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public UUID getEntryId() { return entryId; }
    public long getCreatedAtEpochMs() { return createdAtEpochMs; }
    public long getAmountCents() { return amountCents; }
    public UUID getCounterpartyAccountId() { return counterpartyAccountId; }
    public long getSeq() { return seq; }
    public String getPrevHash() { return prevHash; }
    public String getItemHash() { return itemHash; }
}
