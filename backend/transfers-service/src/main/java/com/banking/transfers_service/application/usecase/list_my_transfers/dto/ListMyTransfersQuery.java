package com.banking.transfers_service.application.usecase.list_my_transfers.dto;

import com.banking.transfers_service.domain.model.TransferStatus;

import java.util.UUID;

public record ListMyTransfersQuery(
        UUID requesterUserId,
        TransferStatus status,
        UUID fromAccountId,
        UUID toAccountId,
        int limit,
        int offset
) {}
