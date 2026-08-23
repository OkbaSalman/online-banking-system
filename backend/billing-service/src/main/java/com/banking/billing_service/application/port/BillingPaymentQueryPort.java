package com.banking.billing_service.application.port;

import com.banking.billing_service.domain.model.BillingPayment;

import java.util.List;
import java.util.UUID;

public interface BillingPaymentQueryPort {
    List<BillingPayment> listByUserId(UUID userId, int limit, int offset);

    List<BillingPayment> listBySubscriptionId(UUID subscriptionId, int limit, int offset);
}
