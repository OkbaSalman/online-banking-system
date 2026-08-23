package com.banking.accounts_service.adapter.out.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "account_invitations")
public class AccountInvitationEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private UUID invitedUserId;

    @Column(nullable = false)
    private UUID invitedByUserId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private long createdAtEpochMs;

    @Column(nullable = false)
    private long expiresAtEpochMs;

    @Column
    private Long respondedAtEpochMs;

    @Column(nullable = false)
    private String invitedByEmail = "";

    protected AccountInvitationEntity() {}

    public AccountInvitationEntity(
            UUID id,
            UUID accountId,
            UUID invitedUserId,
            UUID invitedByUserId,
            String role,
            String status,
            long createdAtEpochMs,
            long expiresAtEpochMs,
            Long respondedAtEpochMs,
            String invitedByEmail
    ) {
        this.id = id;
        this.accountId = accountId;
        this.invitedUserId = invitedUserId;
        this.invitedByUserId = invitedByUserId;
        this.role = role;
        this.status = status;
        this.createdAtEpochMs = createdAtEpochMs;
        this.expiresAtEpochMs = expiresAtEpochMs;
        this.respondedAtEpochMs = respondedAtEpochMs;
        this.invitedByEmail = invitedByEmail == null ? "" : invitedByEmail;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getInvitedUserId() {
        return invitedUserId;
    }

    public UUID getInvitedByUserId() {
        return invitedByUserId;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public long getCreatedAtEpochMs() {
        return createdAtEpochMs;
    }

    public long getExpiresAtEpochMs() {
        return expiresAtEpochMs;
    }

    public Long getRespondedAtEpochMs() {
        return respondedAtEpochMs;
    }

    public String getInvitedByEmail() {
        return invitedByEmail == null ? "" : invitedByEmail;
    }
}
