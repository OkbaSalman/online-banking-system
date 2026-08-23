package com.banking.billing_service.application.port;

import com.banking.billing_service.domain.model.Subscription;

import java.util.List;
import java.util.UUID;

public interface SubscriptionQueryPort {
    List<Subscription> listByUserId(UUID userId, int limit, int offset);

    List<Subscription> listDueActive(long nowEpochMs, int limit);
}
