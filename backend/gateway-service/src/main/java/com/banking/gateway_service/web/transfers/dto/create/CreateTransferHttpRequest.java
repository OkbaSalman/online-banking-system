package com.banking.gateway_service.web.transfers.dto.create;

public record CreateTransferHttpRequest(
        String fromAccountId,
        String toAccountId,
        long amountCents,
        String idempotencyKey,
        String description
) {}
