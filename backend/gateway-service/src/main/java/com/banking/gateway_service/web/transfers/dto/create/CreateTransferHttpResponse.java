package com.banking.gateway_service.web.transfers.dto.create;

import com.banking.gateway_service.web.ledger.dto.common.LedgerEntryHttpDto;
import com.banking.gateway_service.web.transfers.dto.transfer.TransferHttpDto;

public record CreateTransferHttpResponse(
        TransferHttpDto transfer,
        LedgerEntryHttpDto entry,
        long fromBalanceCents,
        long toBalanceCents
) {}
