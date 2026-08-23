package com.banking.gateway_service.web.ledger.dto.transfer;

public record CreateLedgerTransferHttpRequest(
        String fromAccountId,
        String toAccountId,
        long amountCents,
        String idempotencyKey,
        String description
) {}
