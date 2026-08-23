package com.banking.billing_service.application.usecase.get_subscription;

import com.banking.billing_service.application.usecase.get_subscription.dto.GetSubscriptionQuery;
import com.banking.billing_service.application.usecase.get_subscription.dto.GetSubscriptionResult;

public interface GetSubscriptionUseCase {
    GetSubscriptionResult get(GetSubscriptionQuery query);
}
