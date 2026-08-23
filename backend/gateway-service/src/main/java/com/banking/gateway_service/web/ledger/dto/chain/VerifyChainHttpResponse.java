package com.banking.gateway_service.web.ledger.dto.chain;

public record VerifyChainHttpResponse(
        boolean ok,
        long firstInvalidSeq,
        String message
) {}
