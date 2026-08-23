package com.banking.billing_service.application.usecase.create_subscription;

import com.banking.billing_service.application.port.KycClientPort;
import com.banking.billing_service.application.port.KycStatus;
import com.banking.billing_service.application.port.SubscriptionRepositoryPort;
import com.banking.billing_service.application.usecase.create_subscription.dto.CreateSubscriptionCommand;
import com.banking.billing_service.application.usecase.create_subscription.dto.CreateSubscriptionResult;
import com.banking.billing_service.application.usecase.pay_bill.PayBillUseCase;
import com.banking.billing_service.application.usecase.pay_bill.dto.PayBillCommand;
import com.banking.billing_service.domain.model.BillingInterval;
import com.banking.billing_service.domain.model.BillingPaymentStatus;
import com.banking.billing_service.domain.model.Subscription;
import com.banking.billing_service.domain.model.SubscriptionStatus;

import java.util.Optional;
import java.util.UUID;

public class CreateSubscriptionService implements CreateSubscriptionUseCase {

    private final SubscriptionRepositoryPort subscriptions;
    private final KycClientPort kyc;
    private final PayBillUseCase payBill;

    public CreateSubscriptionService(
            SubscriptionRepositoryPort subscriptions,
            KycClientPort kyc,
            PayBillUseCase payBill
    ) {
        this.subscriptions = subscriptions;
        this.kyc = kyc;
        this.payBill = payBill;
    }

    @Override
    public CreateSubscriptionResult create(CreateSubscriptionCommand command) {
        validate(command);

        Optional<Subscription> existing = subscriptions.findByUserIdAndIdempotencyKey(command.userId(), command.idempotencyKey());
        if (existing.isPresent()) {
            return new CreateSubscriptionResult(existing.get());
        }

        KycStatus status = kyc.getMyKycStatus();
        if (status != KycStatus.APPROVED) {
            throw new IllegalArgumentException("KYC not approved");
        }

        long now = System.currentTimeMillis();
        UUID id = UUID.randomUUID();

        Subscription sub = new Subscription(
                id,
                command.userId(),
                command.fromAccountId(),
                command.merchantAccountId(),
                command.amountCents(),
                command.intervalUnit(),
                command.intervalCount(),
                now,
                SubscriptionStatus.ACTIVE,
                now,
                command.idempotencyKey(),
                command.description(),
                null,
                0,
                now
        );
        subscriptions.save(sub);

        String chargeIdempotencyKey = "subcharge:" + sub.id() + ":due:" + now;
        try {
            var result = payBill.pay(new PayBillCommand(
                    sub.userId(),
                    sub.fromAccountId(),
                    sub.merchantAccountId(),
                    sub.amountCents(),
                    chargeIdempotencyKey,
                    sub.description(),
                    sub.id()
            ));

            if (result.payment().status() == BillingPaymentStatus.COMPLETED) {
                long next = BillingInterval.add(now, sub.intervalUnit(), sub.intervalCount());
                sub = new Subscription(
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
                );
                subscriptions.save(sub);
                return new CreateSubscriptionResult(sub);
            }

            String message = result.payment().failureMessage();
            if (message == null || message.isBlank()) {
                message = "First subscription charge failed (" + result.payment().status() + ")";
            }
            recordFailedFirstCharge(sub, now);
            throw new IllegalArgumentException(message);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            recordFailedFirstCharge(sub, now);
            String message = ex.getMessage();
            if (ex instanceof io.grpc.StatusRuntimeException sre && sre.getStatus().getDescription() != null) {
                message = sre.getStatus().getDescription();
            }
            throw new IllegalArgumentException(message == null || message.isBlank()
                    ? "First subscription charge failed"
                    : message);
        }
    }

    private void recordFailedFirstCharge(Subscription sub, long now) {
        subscriptions.save(new Subscription(
                sub.id(),
                sub.userId(),
                sub.fromAccountId(),
                sub.merchantAccountId(),
                sub.amountCents(),
                sub.intervalUnit(),
                sub.intervalCount(),
                now,
                SubscriptionStatus.ACTIVE,
                sub.createdAtEpochMs(),
                sub.idempotencyKey(),
                sub.description(),
                now,
                1,
                now
        ));
    }

    private static void validate(CreateSubscriptionCommand command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        if (command.fromAccountId() == null) {
            throw new IllegalArgumentException("from_account_id is required");
        }
        if (command.merchantAccountId() == null) {
            throw new IllegalArgumentException("merchant_account_id is required");
        }
        if (command.amountCents() <= 0) {
            throw new IllegalArgumentException("amount_cents must be > 0");
        }
        if (command.intervalUnit() == null) {
            throw new IllegalArgumentException("interval_unit is required");
        }
        if (command.intervalCount() <= 0) {
            throw new IllegalArgumentException("interval_count must be > 0");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotency_key is required");
        }
        if (command.startAtEpochMs() < 0) {
            throw new IllegalArgumentException("start_at_epoch_ms must be >= 0");
        }
    }
}
