package com.banking.gateway_service.web.accounts.dto.candebit;

public record CanDebitHttpResponse(
        boolean allowed,
        String reason,
        String accountType
) {}
