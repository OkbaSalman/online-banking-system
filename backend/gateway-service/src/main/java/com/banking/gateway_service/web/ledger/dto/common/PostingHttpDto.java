package com.banking.gateway_service.web.ledger.dto.common;

public record PostingHttpDto(
        String accountId,
        long amountCents
) {}
