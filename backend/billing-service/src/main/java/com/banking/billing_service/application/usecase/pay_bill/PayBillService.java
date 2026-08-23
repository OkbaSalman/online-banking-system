package com.banking.billing_service.application.usecase.pay_bill;

import com.banking.billing_service.application.port.BillingPaymentRepositoryPort;
import com.banking.billing_service.application.port.KycClientPort;
import com.banking.billing_service.application.port.KycStatus;
import com.banking.billing_service.application.port.TransfersClientPort;
import com.banking.billing_service.application.usecase.pay_bill.dto.PayBillCommand;
import com.banking.billing_service.application.usecase.pay_bill.dto.PayBillResult;
import com.banking.billing_service.domain.model.BillingPayment;
import com.banking.billing_service.domain.model.BillingPaymentStatus;
import com.banking.transfers.v1.TransferStatus;

import java.util.Optional;
import java.util.UUID;

public class PayBillService implements PayBillUseCase {

    private final BillingPaymentRepositoryPort payments;
    private final TransfersClientPort transfers;
    private final KycClientPort kyc;

    public PayBillService(
            BillingPaymentRepositoryPort payments,
            TransfersClientPort transfers,
            KycClientPort kyc
    ) {
        this.payments = payments;
        this.transfers = transfers;
        this.kyc = kyc;
    }

    @Override
    public PayBillResult pay(PayBillCommand command) {
        validate(command);

        Optional<BillingPayment> existing = payments.findByUserIdAndIdempotencyKey(command.userId(), command.idempotencyKey());
        if (existing.isPresent()) {
            BillingPayment payment = existing.get();
            if (payment.status() == BillingPaymentStatus.COMPLETED
                    || payment.status() == BillingPaymentStatus.BLOCKED
                    || payment.status() == BillingPaymentStatus.PENDING) {
                return new PayBillResult(payment);
            }
            // FAILED — retry the underlying transfer with a fresh transfer idempotency key.
            return settleTransfer(payment, command);
        }

        long now = System.currentTimeMillis();
        UUID paymentId = UUID.randomUUID();

        KycStatus kycStatus = kyc.getMyKycStatus();
        if (kycStatus != KycStatus.APPROVED) {
            BillingPayment blocked = new BillingPayment(
                    paymentId,
                    command.userId(),
                    command.fromAccountId(),
                    command.merchantAccountId(),
                    command.amountCents(),
                    now,
                    BillingPaymentStatus.BLOCKED,
                    command.idempotencyKey(),
                    command.description(),
                    null,
                    "KYC not approved",
                    command.subscriptionId()
            );
            payments.save(blocked);
            return new PayBillResult(blocked);
        }

        BillingPayment pending = new BillingPayment(
                paymentId,
                command.userId(),
                command.fromAccountId(),
                command.merchantAccountId(),
                command.amountCents(),
                now,
                BillingPaymentStatus.PENDING,
                command.idempotencyKey(),
                command.description(),
                null,
                null,
                command.subscriptionId()
        );
        payments.save(pending);
        return settleTransfer(pending, command);
    }

    private PayBillResult settleTransfer(BillingPayment payment, PayBillCommand command) {
        long now = payment.createdAtEpochMs() > 0 ? payment.createdAtEpochMs() : System.currentTimeMillis();
        try {
            String transferIdem = "billing:" + payment.id() + ":" + System.currentTimeMillis();
            var transfer = transfers.createTransfer(
                    command.fromAccountId(),
                    command.merchantAccountId(),
                    command.amountCents(),
                    transferIdem,
                    command.description()
            );

            BillingPaymentStatus status = switch (transfer.getStatus()) {
                case TRANSFER_STATUS_COMPLETED -> BillingPaymentStatus.COMPLETED;
                case TRANSFER_STATUS_BLOCKED -> BillingPaymentStatus.BLOCKED;
                case TRANSFER_STATUS_FAILED -> BillingPaymentStatus.FAILED;
                case TRANSFER_STATUS_PENDING, TRANSFER_STATUS_UNSPECIFIED, UNRECOGNIZED -> BillingPaymentStatus.FAILED;
            };

            UUID transferId = transfer.getId() == null || transfer.getId().isBlank() ? null : UUID.fromString(transfer.getId());
            String failureMessage = transfer.getFailureMessage() == null || transfer.getFailureMessage().isBlank()
                    ? null
                    : transfer.getFailureMessage();

            BillingPayment completed = new BillingPayment(
                    payment.id(),
                    payment.userId(),
                    payment.fromAccountId(),
                    payment.merchantAccountId(),
                    payment.amountCents(),
                    now,
                    status,
                    payment.idempotencyKey(),
                    payment.description(),
                    transferId,
                    failureMessage,
                    payment.subscriptionId()
            );
            payments.save(completed);
            return new PayBillResult(completed);
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (e instanceof io.grpc.StatusRuntimeException sre && sre.getStatus().getDescription() != null) {
                message = sre.getStatus().getDescription();
            }

            BillingPayment failed = new BillingPayment(
                    payment.id(),
                    payment.userId(),
                    payment.fromAccountId(),
                    payment.merchantAccountId(),
                    payment.amountCents(),
                    now,
                    BillingPaymentStatus.FAILED,
                    payment.idempotencyKey(),
                    payment.description(),
                    null,
                    message,
                    payment.subscriptionId()
            );
            payments.save(failed);
            return new PayBillResult(failed);
        }
    }

    private static void validate(PayBillCommand command) {
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
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotency_key is required");
        }
    }
}
