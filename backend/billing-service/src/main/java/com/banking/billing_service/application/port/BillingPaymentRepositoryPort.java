package com.banking.billing_service.application.port;

import com.banking.billing_service.domain.model.BillingPayment;

import java.util.Optional;
import java.util.UUID;

public interface BillingPaymentRepositoryPort {
    Optional<BillingPayment> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    BillingPayment save(BillingPayment payment);
}
