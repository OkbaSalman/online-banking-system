package com.banking.billing_service.application.usecase.get_subscription.dto;

import com.banking.billing_service.domain.model.Subscription;

public record GetSubscriptionResult(
        Subscription subscription
) {}
