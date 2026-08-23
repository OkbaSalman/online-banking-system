package com.banking.billing_service.adapter.in.grpc;

import com.banking.billing.v1.*;
import com.banking.billing_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import com.banking.billing_service.application.usecase.cancel_subscription.CancelSubscriptionUseCase;
import com.banking.billing_service.application.usecase.cancel_subscription.dto.CancelSubscriptionCommand;
import com.banking.billing_service.application.usecase.create_subscription.CreateSubscriptionUseCase;
import com.banking.billing_service.application.usecase.create_subscription.dto.CreateSubscriptionCommand;
import com.banking.billing_service.application.usecase.get_subscription.GetSubscriptionUseCase;
import com.banking.billing_service.application.usecase.get_subscription.dto.GetSubscriptionQuery;
import com.banking.billing_service.application.usecase.list_my_payments.ListMyPaymentsUseCase;
import com.banking.billing_service.application.usecase.list_my_payments.dto.ListMyPaymentsQuery;
import com.banking.billing_service.application.usecase.list_my_subscriptions.ListMySubscriptionsUseCase;
import com.banking.billing_service.application.usecase.list_my_subscriptions.dto.ListMySubscriptionsQuery;
import com.banking.billing_service.application.usecase.pay_bill.PayBillUseCase;
import com.banking.billing_service.application.usecase.pay_bill.dto.PayBillCommand;
import com.banking.billing_service.domain.model.BillingPayment;
import com.banking.billing_service.domain.model.BillingPaymentStatus;
import com.banking.billing_service.domain.model.IntervalUnit;
import com.banking.billing_service.domain.model.Subscription;
import com.banking.billing_service.domain.model.SubscriptionStatus;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BillingGrpcController extends BillingServiceGrpc.BillingServiceImplBase {

    private final PayBillUseCase payBillUseCase;
    private final CreateSubscriptionUseCase createSubscriptionUseCase;
    private final CancelSubscriptionUseCase cancelSubscriptionUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final ListMySubscriptionsUseCase listMySubscriptionsUseCase;
    private final ListMyPaymentsUseCase listMyPaymentsUseCase;

    public BillingGrpcController(
            PayBillUseCase payBillUseCase,
            CreateSubscriptionUseCase createSubscriptionUseCase,
            CancelSubscriptionUseCase cancelSubscriptionUseCase,
            GetSubscriptionUseCase getSubscriptionUseCase,
            ListMySubscriptionsUseCase listMySubscriptionsUseCase,
            ListMyPaymentsUseCase listMyPaymentsUseCase
    ) {
        this.payBillUseCase = payBillUseCase;
        this.createSubscriptionUseCase = createSubscriptionUseCase;
        this.cancelSubscriptionUseCase = cancelSubscriptionUseCase;
        this.getSubscriptionUseCase = getSubscriptionUseCase;
        this.listMySubscriptionsUseCase = listMySubscriptionsUseCase;
        this.listMyPaymentsUseCase = listMyPaymentsUseCase;
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().setMessage("pong: " + request.getMessage()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void payBill(PayBillRequest request, StreamObserver<PayBillResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = payBillUseCase.pay(new PayBillCommand(
                    userId,
                    UUID.fromString(request.getFromAccountId()),
                    UUID.fromString(request.getMerchantAccountId()),
                    request.getAmountCents(),
                    request.getIdempotencyKey(),
                    request.getDescription(),
                    null
            ));

            responseObserver.onNext(PayBillResponse.newBuilder().setPayment(toProto(res.payment())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void createSubscription(CreateSubscriptionRequest request, StreamObserver<CreateSubscriptionResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = createSubscriptionUseCase.create(new CreateSubscriptionCommand(
                    userId,
                    UUID.fromString(request.getFromAccountId()),
                    UUID.fromString(request.getMerchantAccountId()),
                    request.getAmountCents(),
                    toDomainIntervalUnit(request.getIntervalUnit()),
                    request.getIntervalCount(),
                    request.getStartAtEpochMs(),
                    request.getIdempotencyKey(),
                    request.getDescription()
            ));

            responseObserver.onNext(CreateSubscriptionResponse.newBuilder().setSubscription(toProto(res.subscription())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void cancelSubscription(CancelSubscriptionRequest request, StreamObserver<CancelSubscriptionResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = cancelSubscriptionUseCase.cancel(new CancelSubscriptionCommand(
                    userId,
                    UUID.fromString(request.getSubscriptionId())
            ));

            responseObserver.onNext(CancelSubscriptionResponse.newBuilder().setSubscription(toProto(res.subscription())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void getSubscription(GetSubscriptionRequest request, StreamObserver<GetSubscriptionResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = getSubscriptionUseCase.get(new GetSubscriptionQuery(
                    userId,
                    UUID.fromString(request.getSubscriptionId())
            ));

            responseObserver.onNext(GetSubscriptionResponse.newBuilder().setSubscription(toProto(res.subscription())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listMySubscriptions(ListMySubscriptionsRequest request, StreamObserver<ListMySubscriptionsResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = listMySubscriptionsUseCase.list(new ListMySubscriptionsQuery(
                    userId,
                    request.getLimit(),
                    request.getOffset()
            ));

            var b = ListMySubscriptionsResponse.newBuilder();
            for (Subscription s : res.subscriptions()) {
                b.addSubscriptions(toProto(s));
            }

            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listMyPayments(ListMyPaymentsRequest request, StreamObserver<ListMyPaymentsResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = listMyPaymentsUseCase.list(new ListMyPaymentsQuery(
                    userId,
                    request.getLimit(),
                    request.getOffset()
            ));

            var b = ListMyPaymentsResponse.newBuilder();
            for (BillingPayment p : res.payments()) {
                b.addPayments(toProto(p));
            }

            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    private static com.banking.billing.v1.BillingPayment toProto(BillingPayment p) {
        var b = com.banking.billing.v1.BillingPayment.newBuilder()
                .setId(p.id().toString())
                .setUserId(p.userId().toString())
                .setFromAccountId(p.fromAccountId().toString())
                .setMerchantAccountId(p.merchantAccountId().toString())
                .setAmountCents(p.amountCents())
                .setCreatedAtEpochMs(p.createdAtEpochMs())
                .setStatus(toProtoStatus(p.status()))
                .setIdempotencyKey(p.idempotencyKey())
                .setDescription(p.description() == null ? "" : p.description())
                .setTransferId(p.transferId() == null ? "" : p.transferId().toString())
                .setFailureMessage(p.failureMessage() == null ? "" : p.failureMessage())
                .setSubscriptionId(p.subscriptionId() == null ? "" : p.subscriptionId().toString());

        return b.build();
    }

    private static com.banking.billing.v1.Subscription toProto(Subscription s) {
        return com.banking.billing.v1.Subscription.newBuilder()
                .setId(s.id().toString())
                .setUserId(s.userId().toString())
                .setFromAccountId(s.fromAccountId().toString())
                .setMerchantAccountId(s.merchantAccountId().toString())
                .setAmountCents(s.amountCents())
                .setIntervalUnit(toProtoIntervalUnit(s.intervalUnit()))
                .setIntervalCount(s.intervalCount())
                .setNextChargeAtEpochMs(s.nextChargeAtEpochMs())
                .setStatus(toProtoSubscriptionStatus(s.status()))
                .setCreatedAtEpochMs(s.createdAtEpochMs())
                .setIdempotencyKey(s.idempotencyKey())
                .setDescription(s.description() == null ? "" : s.description())
                .build();
    }

    private static com.banking.billing.v1.BillingPaymentStatus toProtoStatus(BillingPaymentStatus status) {
        return switch (status) {
            case PENDING -> com.banking.billing.v1.BillingPaymentStatus.BILLING_PAYMENT_STATUS_PENDING;
            case COMPLETED -> com.banking.billing.v1.BillingPaymentStatus.BILLING_PAYMENT_STATUS_COMPLETED;
            case BLOCKED -> com.banking.billing.v1.BillingPaymentStatus.BILLING_PAYMENT_STATUS_BLOCKED;
            case FAILED -> com.banking.billing.v1.BillingPaymentStatus.BILLING_PAYMENT_STATUS_FAILED;
        };
    }

    private static com.banking.billing.v1.SubscriptionStatus toProtoSubscriptionStatus(SubscriptionStatus status) {
        return switch (status) {
            case ACTIVE -> com.banking.billing.v1.SubscriptionStatus.SUBSCRIPTION_STATUS_ACTIVE;
            case PAUSED -> com.banking.billing.v1.SubscriptionStatus.SUBSCRIPTION_STATUS_PAUSED;
            case CANCELED -> com.banking.billing.v1.SubscriptionStatus.SUBSCRIPTION_STATUS_CANCELED;
        };
    }

    private static com.banking.billing.v1.IntervalUnit toProtoIntervalUnit(IntervalUnit unit) {
        return switch (unit) {
            case DAY -> com.banking.billing.v1.IntervalUnit.INTERVAL_UNIT_DAY;
            case WEEK -> com.banking.billing.v1.IntervalUnit.INTERVAL_UNIT_WEEK;
            case MONTH -> com.banking.billing.v1.IntervalUnit.INTERVAL_UNIT_MONTH;
        };
    }

    private static IntervalUnit toDomainIntervalUnit(com.banking.billing.v1.IntervalUnit unit) {
        return switch (unit) {
            case INTERVAL_UNIT_DAY -> IntervalUnit.DAY;
            case INTERVAL_UNIT_WEEK -> IntervalUnit.WEEK;
            case INTERVAL_UNIT_MONTH -> IntervalUnit.MONTH;
            case INTERVAL_UNIT_UNSPECIFIED, UNRECOGNIZED -> throw new IllegalArgumentException("interval_unit is required");
        };
    }
}
