package com.banking.billing_service.application.usecase.pay_bill.dto;

import java.util.UUID;

public record PayBillCommand(
        UUID userId,
        UUID fromAccountId,
        UUID merchantAccountId,
        long amountCents,
        String idempotencyKey,
        String description,
        UUID subscriptionId
) {}
