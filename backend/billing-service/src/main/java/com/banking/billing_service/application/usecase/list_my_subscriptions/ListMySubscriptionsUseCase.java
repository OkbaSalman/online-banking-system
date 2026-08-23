package com.banking.billing_service.application.usecase.list_my_subscriptions;

import com.banking.billing_service.application.usecase.list_my_subscriptions.dto.ListMySubscriptionsQuery;
import com.banking.billing_service.application.usecase.list_my_subscriptions.dto.ListMySubscriptionsResult;

public interface ListMySubscriptionsUseCase {
    ListMySubscriptionsResult list(ListMySubscriptionsQuery query);
}
