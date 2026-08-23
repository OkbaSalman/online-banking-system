package com.banking.billing_service.application.usecase.cancel_subscription.dto;

import java.util.UUID;

public record CancelSubscriptionCommand(
        UUID userId,
        UUID subscriptionId
) {}
