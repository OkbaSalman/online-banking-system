package com.banking.transfers_service.application.usecase.admin_mint.dto;

import java.util.UUID;

public record AdminMintCommand(
        UUID initiatorUserId,
        UUID toAccountId,
        long amountCents,
        String idempotencyKey,
        String description
) {}
