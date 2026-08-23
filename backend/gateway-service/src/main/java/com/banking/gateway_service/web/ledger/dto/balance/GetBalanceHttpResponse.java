package com.banking.gateway_service.web.ledger.dto.balance;

public record GetBalanceHttpResponse(
        String accountId,
        long availableCents
) {}
