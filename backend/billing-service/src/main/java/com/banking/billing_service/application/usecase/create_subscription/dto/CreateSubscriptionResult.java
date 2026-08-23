package com.banking.billing_service.application.usecase.create_subscription.dto;

import com.banking.billing_service.domain.model.Subscription;

public record CreateSubscriptionResult(
        Subscription subscription
) {}
