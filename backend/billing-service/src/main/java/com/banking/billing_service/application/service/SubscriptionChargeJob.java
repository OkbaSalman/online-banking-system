package com.banking.billing_service.application.service;

import com.banking.billing_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import com.banking.billing_service.application.port.SubscriptionQueryPort;
import com.banking.billing_service.application.port.SubscriptionRepositoryPort;
import com.banking.billing_service.application.usecase.pay_bill.PayBillUseCase;
import com.banking.billing_service.application.usecase.pay_bill.dto.PayBillCommand;
import com.banking.billing_service.domain.model.BillingInterval;
import com.banking.billing_service.domain.model.BillingPaymentStatus;
import com.banking.billing_service.domain.model.Subscription;
import com.banking.billing_service.domain.model.SubscriptionStatus;
import io.grpc.Context;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionChargeJob {

    private static final int MAX_CONSECUTIVE_FAILURES = 5;

    private final SubscriptionQueryPort subscriptionQueryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final PayBillUseCase payBillUseCase;

    private final boolean enabled;
    private final int batchSize;
    private final boolean catchUp;
    private final long retryDelayMs;

    public SubscriptionChargeJob(
            SubscriptionQueryPort subscriptionQueryPort,
            SubscriptionRepositoryPort subscriptionRepositoryPort,
            PayBillUseCase payBillUseCase,
            @Value("${billing.scheduler.enabled:false}") boolean enabled,
            @Value("${billing.scheduler.batch-size:25}") int batchSize,
            @Value("${billing.scheduler.catch-up:false}") boolean catchUp,
            @Value("${billing.scheduler.retry-ms:60000}") long retryDelayMs
    ) {
        this.subscriptionQueryPort = subscriptionQueryPort;
        this.subscriptionRepositoryPort = subscriptionRepositoryPort;
        this.payBillUseCase = payBillUseCase;
        this.enabled = enabled;
        this.batchSize = batchSize;
        this.catchUp = catchUp;
        this.retryDelayMs = retryDelayMs;
    }

    @Scheduled(fixedDelayString = "${billing.scheduler.poll-ms:10000}")
    public void pollAndCharge() {
        if (!enabled) {
            return;
        }

        long now = System.currentTimeMillis();
        long retryMs = Math.max(retryDelayMs, 1000);

        for (Subscription sub : subscriptionQueryPort.listDueActive(now, batchSize)) {
            if (sub.status() != SubscriptionStatus.ACTIVE) {
                continue;
            }
            if (sub.lastAttemptAtEpochMs() != null && now - sub.lastAttemptAtEpochMs() < retryMs) {
                continue;
            }

            Context.current()
                    .withValue(AuthMetadataServerInterceptor.USER_ID_CTX_KEY, sub.userId())
                    .withValue(AuthMetadataServerInterceptor.ROLE_CTX_KEY, "USER")
                    .run(() -> chargeOne(sub, now));
        }
    }

    private void chargeOne(Subscription sub, long now) {
        long dueAnchor = sub.dueAnchorEpochMs() > 0 ? sub.dueAnchorEpochMs() : sub.nextChargeAtEpochMs();
        // Stable per billing period so retries don't create a new payment every minute.
        String idempotencyKey = "subcharge:" + sub.id() + ":due:" + dueAnchor;

        try {
            var result = payBillUseCase.pay(new PayBillCommand(
                    sub.userId(),
                    sub.fromAccountId(),
                    sub.merchantAccountId(),
                    sub.amountCents(),
                    idempotencyKey,
                    sub.description(),
                    sub.id()
            ));

            if (result.payment().status() == BillingPaymentStatus.COMPLETED) {
                long next = computeNextChargeAt(dueAnchor, sub, now);
                subscriptionRepositoryPort.save(new Subscription(
                        sub.id(),
                        sub.userId(),
                        sub.fromAccountId(),
                        sub.merchantAccountId(),
                        sub.amountCents(),
                        sub.intervalUnit(),
                        sub.intervalCount(),
                        next,
                        SubscriptionStatus.ACTIVE,
                        sub.createdAtEpochMs(),
                        sub.idempotencyKey(),
                        sub.description(),
                        now,
                        0,
                        next
                ));
                return;
            }

            recordFailure(sub, dueAnchor, now);
        } catch (Throwable t) {
            recordFailure(sub, dueAnchor, now);
        }
    }

    private void recordFailure(Subscription sub, long dueAnchor, long now) {
        int failures = sub.consecutiveFailures() + 1;
        SubscriptionStatus status = failures >= MAX_CONSECUTIVE_FAILURES
                ? SubscriptionStatus.PAUSED
                : SubscriptionStatus.ACTIVE;

        // Keep nextChargeAt at the due anchor for display; backoff is enforced via lastAttemptAt.
        subscriptionRepositoryPort.save(new Subscription(
                sub.id(),
                sub.userId(),
                sub.fromAccountId(),
                sub.merchantAccountId(),
                sub.amountCents(),
                sub.intervalUnit(),
                sub.intervalCount(),
                dueAnchor,
                status,
                sub.createdAtEpochMs(),
                sub.idempotencyKey(),
                sub.description(),
                now,
                failures,
                dueAnchor
        ));
    }

    private long computeNextChargeAt(long dueAnchor, Subscription sub, long nowEpochMs) {
        if (!catchUp) {
            return BillingInterval.add(Math.max(dueAnchor, nowEpochMs), sub.intervalUnit(), sub.intervalCount());
        }

        long next = dueAnchor;
        do {
            next = BillingInterval.add(next, sub.intervalUnit(), sub.intervalCount());
        } while (next <= nowEpochMs);

        return next;
    }
}
