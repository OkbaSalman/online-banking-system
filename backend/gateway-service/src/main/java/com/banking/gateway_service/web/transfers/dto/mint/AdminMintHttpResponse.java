package com.banking.gateway_service.web.transfers.dto.mint;

import com.banking.gateway_service.web.ledger.dto.common.LedgerEntryHttpDto;
import com.banking.gateway_service.web.transfers.dto.transfer.TransferHttpDto;

public record AdminMintHttpResponse(
        TransferHttpDto transfer,
        LedgerEntryHttpDto entry,
        long treasuryBalanceCents,
        long toBalanceCents
) {}
