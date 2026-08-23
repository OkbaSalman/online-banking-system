package com.banking.billing_service.application.usecase.get_subscription;

import com.banking.billing_service.application.port.SubscriptionRepositoryPort;
import com.banking.billing_service.application.usecase.common.exception.NotFoundException;
import com.banking.billing_service.application.usecase.get_subscription.dto.GetSubscriptionQuery;
import com.banking.billing_service.application.usecase.get_subscription.dto.GetSubscriptionResult;

public class GetSubscriptionService implements GetSubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptions;

    public GetSubscriptionService(SubscriptionRepositoryPort subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public GetSubscriptionResult get(GetSubscriptionQuery query) {
        if (query.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        if (query.subscriptionId() == null) {
            throw new IllegalArgumentException("subscription_id is required");
        }

        var sub = subscriptions.findById(query.subscriptionId())
                .orElseThrow(() -> new NotFoundException("Subscription not found"));

        if (!query.userId().equals(sub.userId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        return new GetSubscriptionResult(sub);
    }
}
