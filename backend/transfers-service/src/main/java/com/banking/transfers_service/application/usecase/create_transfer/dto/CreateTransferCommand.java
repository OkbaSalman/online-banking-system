package com.banking.transfers_service.application.usecase.create_transfer.dto;

import java.util.UUID;

public record CreateTransferCommand(
        UUID initiatorUserId,
        UUID fromAccountId,
        UUID toAccountId,
        long amountCents,
        String idempotencyKey,
        String description
) {}
