package com.banking.ledger_service.adapter.out.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ledger_chain_heads")
public class LedgerChainHeadEntity {

    @Id
    private UUID accountId;

    private long headSeq;

    private String headHash;

    private UUID headEntryId;

    protected LedgerChainHeadEntity() {}

    public LedgerChainHeadEntity(UUID accountId, long headSeq, String headHash, UUID headEntryId) {
        this.accountId = accountId;
        this.headSeq = headSeq;
        this.headHash = headHash;
        this.headEntryId = headEntryId;
    }

    public UUID getAccountId() { return accountId; }
    public long getHeadSeq() { return headSeq; }
    public String getHeadHash() { return headHash; }
    public UUID getHeadEntryId() { return headEntryId; }

    public void setHeadSeq(long headSeq) { this.headSeq = headSeq; }
    public void setHeadHash(String headHash) { this.headHash = headHash; }
    public void setHeadEntryId(UUID headEntryId) { this.headEntryId = headEntryId; }
}