package com.banking.billing_service.application.usecase.list_my_subscriptions;

import com.banking.billing_service.application.port.SubscriptionQueryPort;
import com.banking.billing_service.application.usecase.list_my_subscriptions.dto.ListMySubscriptionsQuery;
import com.banking.billing_service.application.usecase.list_my_subscriptions.dto.ListMySubscriptionsResult;

public class ListMySubscriptionsService implements ListMySubscriptionsUseCase {

    private final SubscriptionQueryPort subscriptions;

    public ListMySubscriptionsService(SubscriptionQueryPort subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public ListMySubscriptionsResult list(ListMySubscriptionsQuery query) {
        if (query.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        return new ListMySubscriptionsResult(subscriptions.listByUserId(query.userId(), query.limit(), query.offset()));
    }
}
