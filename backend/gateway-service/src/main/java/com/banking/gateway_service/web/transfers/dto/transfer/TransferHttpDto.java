package com.banking.gateway_service.web.transfers.dto.transfer;

public record TransferHttpDto(
        String id,
        String initiatorUserId,
        String fromAccountId,
        String toAccountId,
        long amountCents,
        String idempotencyKey,
        String description,
        long createdAtEpochMs,
        String status,
        String ledgerEntryId,
        String failureMessage,
        long feeCents,
        String feeLedgerEntryId
) {}
