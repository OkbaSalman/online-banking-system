package com.banking.ledger_service.adapter.out.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "account_balances")
public class AccountBalanceEntity {

    @Id
    private UUID accountId;

    private long availableCents;

    protected AccountBalanceEntity() {}

    public AccountBalanceEntity(UUID accountId, long availableCents) {
        this.accountId = accountId;
        this.availableCents = availableCents;
    }

    public UUID getAccountId() { return accountId; }
    public long getAvailableCents() { return availableCents; }

    public void setAvailableCents(long availableCents) { this.availableCents = availableCents; }
}