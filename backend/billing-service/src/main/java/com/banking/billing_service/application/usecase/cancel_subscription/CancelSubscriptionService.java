package com.banking.billing_service.application.usecase.cancel_subscription;

import com.banking.billing_service.application.port.SubscriptionRepositoryPort;
import com.banking.billing_service.application.usecase.cancel_subscription.dto.CancelSubscriptionCommand;
import com.banking.billing_service.application.usecase.cancel_subscription.dto.CancelSubscriptionResult;
import com.banking.billing_service.application.usecase.common.exception.NotFoundException;
import com.banking.billing_service.domain.model.Subscription;
import com.banking.billing_service.domain.model.SubscriptionStatus;

public class CancelSubscriptionService implements CancelSubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptions;

    public CancelSubscriptionService(SubscriptionRepositoryPort subscriptions) {
        this.subscriptions = subscriptions;
    }

    @Override
    public CancelSubscriptionResult cancel(CancelSubscriptionCommand command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        if (command.subscriptionId() == null) {
            throw new IllegalArgumentException("subscription_id is required");
        }

        Subscription existing = subscriptions.findById(command.subscriptionId())
                .orElseThrow(() -> new NotFoundException("Subscription not found"));

        if (!command.userId().equals(existing.userId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        if (existing.status() == SubscriptionStatus.CANCELED) {
            return new CancelSubscriptionResult(existing);
        }

        Subscription canceled = new Subscription(
                existing.id(),
                existing.userId(),
                existing.fromAccountId(),
                existing.merchantAccountId(),
                existing.amountCents(),
                existing.intervalUnit(),
                existing.intervalCount(),
                existing.nextChargeAtEpochMs(),
                SubscriptionStatus.CANCELED,
                existing.createdAtEpochMs(),
                existing.idempotencyKey(),
                existing.description(),
                existing.lastAttemptAtEpochMs(),
                existing.consecutiveFailures(),
                existing.dueAnchorEpochMs()
        );

        subscriptions.save(canceled);
        return new CancelSubscriptionResult(canceled);
    }
}
