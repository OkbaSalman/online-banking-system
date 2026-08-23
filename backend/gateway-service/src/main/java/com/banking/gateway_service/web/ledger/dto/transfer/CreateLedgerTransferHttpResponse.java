package com.banking.gateway_service.web.ledger.dto.transfer;

import com.banking.gateway_service.web.ledger.dto.common.LedgerEntryHttpDto;

public record CreateLedgerTransferHttpResponse(
        LedgerEntryHttpDto entry,
        long fromBalanceCents,
        long toBalanceCents
) {}
