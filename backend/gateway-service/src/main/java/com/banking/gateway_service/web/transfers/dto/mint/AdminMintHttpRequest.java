package com.banking.gateway_service.web.transfers.dto.mint;

public record AdminMintHttpRequest(
        String toAccountId,
        long amountCents,
        String idempotencyKey,
        String description
) {}
