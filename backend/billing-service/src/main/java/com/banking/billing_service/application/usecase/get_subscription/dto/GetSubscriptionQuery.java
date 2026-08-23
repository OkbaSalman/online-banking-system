package com.banking.billing_service.application.usecase.get_subscription.dto;

import java.util.UUID;

public record GetSubscriptionQuery(
        UUID userId,
        UUID subscriptionId
) {}
