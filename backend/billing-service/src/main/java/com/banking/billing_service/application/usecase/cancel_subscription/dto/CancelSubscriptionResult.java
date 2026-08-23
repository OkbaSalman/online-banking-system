package com.banking.billing_service.application.usecase.cancel_subscription.dto;

import com.banking.billing_service.domain.model.Subscription;

public record CancelSubscriptionResult(
        Subscription subscription
) {}
