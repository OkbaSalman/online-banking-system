package com.banking.transfers_service.application.usecase.admin_list_transfers.dto;

import com.banking.transfers_service.domain.model.TransferStatus;

import java.util.UUID;

public record AdminListTransfersQuery(
        TransferStatus status,
        UUID initiatorUserId,
        UUID fromAccountId,
        UUID toAccountId,
        int limit,
        int offset
) {}
