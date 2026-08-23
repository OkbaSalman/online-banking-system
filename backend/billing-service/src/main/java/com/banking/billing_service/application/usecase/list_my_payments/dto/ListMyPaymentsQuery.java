package com.banking.billing_service.application.usecase.list_my_payments.dto;

import java.util.UUID;

public record ListMyPaymentsQuery(
        UUID userId,
        int limit,
        int offset
) {}
