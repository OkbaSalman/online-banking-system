package com.banking.gateway_service.web.ledger.dto.common;

import java.util.List;

public record LedgerEntryHttpDto(
        String id,
        String initiatorUserId,
        String idempotencyKey,
        String type,
        String description,
        long createdAtEpochMs,
        String fromAccountId,
        String toAccountId,
        long amountCents,
        List<PostingHttpDto> postings
) {}
