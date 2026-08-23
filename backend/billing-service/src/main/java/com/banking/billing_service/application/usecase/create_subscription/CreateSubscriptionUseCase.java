package com.banking.billing_service.application.usecase.create_subscription;

import com.banking.billing_service.application.usecase.create_subscription.dto.CreateSubscriptionCommand;
import com.banking.billing_service.application.usecase.create_subscription.dto.CreateSubscriptionResult;

public interface CreateSubscriptionUseCase {
    CreateSubscriptionResult create(CreateSubscriptionCommand command);
}
