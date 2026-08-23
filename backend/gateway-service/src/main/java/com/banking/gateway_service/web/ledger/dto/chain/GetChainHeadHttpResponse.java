package com.banking.gateway_service.web.ledger.dto.chain;

public record GetChainHeadHttpResponse(
        String accountId,
        long headSeq,
        String headHash,
        String headEntryId
) {}
