package com.banking.billing_service.application.port;

import com.banking.billing_service.domain.model.Subscription;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepositoryPort {
    Optional<Subscription> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    Optional<Subscription> findById(UUID subscriptionId);

    Subscription save(Subscription subscription);
}
