package com.banking.ledger_service.application.usecase.create_transfer.dto;

import java.util.UUID;

public record CreateTransferCommand(
        UUID userId,
        UUID fromAccountId,
        UUID toAccountId,
        long amountCents,
        String idempotencyKey,
        String description
) {}