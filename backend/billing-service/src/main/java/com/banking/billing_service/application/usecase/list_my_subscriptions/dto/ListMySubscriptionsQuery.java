package com.banking.billing_service.application.usecase.list_my_subscriptions.dto;

import java.util.UUID;

public record ListMySubscriptionsQuery(
        UUID userId,
        int limit,
        int offset
) {}
