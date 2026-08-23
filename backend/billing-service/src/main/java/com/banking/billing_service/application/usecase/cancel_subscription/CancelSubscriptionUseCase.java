package com.banking.billing_service.application.usecase.cancel_subscription;

import com.banking.billing_service.application.usecase.cancel_subscription.dto.CancelSubscriptionCommand;
import com.banking.billing_service.application.usecase.cancel_subscription.dto.CancelSubscriptionResult;

public interface CancelSubscriptionUseCase {
    CancelSubscriptionResult cancel(CancelSubscriptionCommand command);
}
