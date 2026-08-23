package com.banking.billing_service.application.usecase.list_my_subscriptions.dto;

import com.banking.billing_service.domain.model.Subscription;

import java.util.List;

public record ListMySubscriptionsResult(
        List<Subscription> subscriptions
) {}
