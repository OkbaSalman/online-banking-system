package com.banking.gateway_service.web.billing;

import com.banking.billing.v1.BillingPayment;
import com.banking.billing.v1.BillingServiceGrpc;
import com.banking.billing.v1.CancelSubscriptionRequest;
import com.banking.billing.v1.CreateSubscriptionRequest;
import com.banking.billing.v1.GetSubscriptionRequest;
import com.banking.billing.v1.ListMyPaymentsRequest;
import com.banking.billing.v1.ListMySubscriptionsRequest;
import com.banking.billing.v1.PayBillRequest;
import com.banking.billing.v1.PingRequest;
import com.banking.billing.v1.Subscription;
import com.banking.gateway_service.grpc.security.GrpcAuthContext;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
public class BillingController {

    private final BillingServiceGrpc.BillingServiceBlockingStub billing;

    public record PingHttpResponse(String message) {}

    public BillingController(BillingServiceGrpc.BillingServiceBlockingStub billing) {
        this.billing = billing;
    }

    public record PayBillHttpRequest(
            String fromAccountId,
            String merchantAccountId,
            long amountCents,
            String idempotencyKey,
            String description
    ) {}

    public record BillingPaymentHttpDto(
            String id,
            String userId,
            String fromAccountId,
            String merchantAccountId,
            long amountCents,
            long createdAtEpochMs,
            String status,
            String idempotencyKey,
            String description,
            String transferId,
            String failureMessage,
            String subscriptionId
    ) {}

    public record SubscriptionHttpDto(
            String id,
            String userId,
            String fromAccountId,
            String merchantAccountId,
            long amountCents,
            String intervalUnit,
            int intervalCount,
            long nextChargeAtEpochMs,
            String status,
            long createdAtEpochMs,
            String idempotencyKey,
            String description
    ) {}

    public record PayBillHttpResponse(BillingPaymentHttpDto payment) {}

    public record CreateSubscriptionHttpRequest(
            String fromAccountId,
            String merchantAccountId,
            long amountCents,
            String intervalUnit,
            int intervalCount,
            long startAtEpochMs,
            String idempotencyKey,
            String description
    ) {}

    public record CreateSubscriptionHttpResponse(SubscriptionHttpDto subscription) {}

    public record CancelSubscriptionHttpResponse(SubscriptionHttpDto subscription) {}

    @GetMapping("/api/billing/ping")
    public Mono<PingHttpResponse> ping(Authentication authentication, @RequestParam(defaultValue = "hello") String message) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> billing.ping(
                PingRequest.newBuilder().setMessage(message).build()
        )))
                .subscribeOn(Schedulers.boundedElastic())
                .map(res -> new PingHttpResponse(res.getMessage()));
    }

    @PostMapping("/api/billing/pay-bill")
    public Mono<PayBillHttpResponse> payBill(Authentication authentication, @RequestBody PayBillHttpRequest body) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> billing.payBill(
                PayBillRequest.newBuilder()
                        .setFromAccountId(body.fromAccountId())
                        .setMerchantAccountId(body.merchantAccountId())
                        .setAmountCents(body.amountCents())
                        .setIdempotencyKey(body.idempotencyKey())
                        .setDescription(body.description() == null ? "" : body.description())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> new PayBillHttpResponse(toHttp(res.getPayment())));
    }

    @PostMapping("/api/billing/subscriptions")
    public Mono<CreateSubscriptionHttpResponse> createSubscription(Authentication authentication, @RequestBody CreateSubscriptionHttpRequest body) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> billing.createSubscription(
                CreateSubscriptionRequest.newBuilder()
                        .setFromAccountId(body.fromAccountId())
                        .setMerchantAccountId(body.merchantAccountId())
                        .setAmountCents(body.amountCents())
                        .setIntervalUnit(parseIntervalUnit(body.intervalUnit()))
                        .setIntervalCount(body.intervalCount())
                        .setStartAtEpochMs(body.startAtEpochMs())
                        .setIdempotencyKey(body.idempotencyKey())
                        .setDescription(body.description() == null ? "" : body.description())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> new CreateSubscriptionHttpResponse(toHttp(res.getSubscription())));
    }

    @PostMapping("/api/billing/subscriptions/{subscriptionId}/cancel")
    public Mono<CancelSubscriptionHttpResponse> cancelSubscription(Authentication authentication, @PathVariable String subscriptionId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> billing.cancelSubscription(
                CancelSubscriptionRequest.newBuilder()
                        .setSubscriptionId(subscriptionId)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> new CancelSubscriptionHttpResponse(toHttp(res.getSubscription())));
    }

    @GetMapping("/api/billing/subscriptions/{subscriptionId}")
    public Mono<SubscriptionHttpDto> getSubscription(Authentication authentication, @PathVariable String subscriptionId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> billing.getSubscription(
                GetSubscriptionRequest.newBuilder().setSubscriptionId(subscriptionId).build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> toHttp(res.getSubscription()));
    }

    @GetMapping("/api/billing/subscriptions")
    public Mono<List<SubscriptionHttpDto>> listMySubscriptions(
            Authentication authentication,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> billing.listMySubscriptions(
                ListMySubscriptionsRequest.newBuilder().setLimit(limit).setOffset(offset).build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> res.getSubscriptionsList().stream().map(BillingController::toHttp).toList());
    }

    @GetMapping("/api/billing/payments")
    public Mono<List<BillingPaymentHttpDto>> listMyPayments(
            Authentication authentication,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> billing.listMyPayments(
                ListMyPaymentsRequest.newBuilder().setLimit(limit).setOffset(offset).build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> res.getPaymentsList().stream().map(BillingController::toHttp).toList());
    }

    private static BillingPaymentHttpDto toHttp(BillingPayment p) {
        return new BillingPaymentHttpDto(
                p.getId(),
                p.getUserId(),
                p.getFromAccountId(),
                p.getMerchantAccountId(),
                p.getAmountCents(),
                p.getCreatedAtEpochMs(),
                toHttpPaymentStatus(p.getStatus()),
                p.getIdempotencyKey(),
                p.getDescription(),
                p.getTransferId(),
                p.getFailureMessage(),
                p.getSubscriptionId()
        );
    }

    private static SubscriptionHttpDto toHttp(Subscription s) {
        return new SubscriptionHttpDto(
                s.getId(),
                s.getUserId(),
                s.getFromAccountId(),
                s.getMerchantAccountId(),
                s.getAmountCents(),
                toHttpIntervalUnit(s.getIntervalUnit()),
                s.getIntervalCount(),
                s.getNextChargeAtEpochMs(),
                toHttpSubscriptionStatus(s.getStatus()),
                s.getCreatedAtEpochMs(),
                s.getIdempotencyKey(),
                s.getDescription()
        );
    }

    private static String toHttpPaymentStatus(com.banking.billing.v1.BillingPaymentStatus status) {
        return switch (status) {
            case BILLING_PAYMENT_STATUS_PENDING -> "PENDING";
            case BILLING_PAYMENT_STATUS_COMPLETED -> "COMPLETED";
            case BILLING_PAYMENT_STATUS_BLOCKED -> "BLOCKED";
            case BILLING_PAYMENT_STATUS_FAILED -> "FAILED";
            case BILLING_PAYMENT_STATUS_UNSPECIFIED, UNRECOGNIZED -> "UNSPECIFIED";
        };
    }

    private static String toHttpSubscriptionStatus(com.banking.billing.v1.SubscriptionStatus status) {
        return switch (status) {
            case SUBSCRIPTION_STATUS_ACTIVE -> "ACTIVE";
            case SUBSCRIPTION_STATUS_PAUSED -> "PAUSED";
            case SUBSCRIPTION_STATUS_CANCELED -> "CANCELED";
            case SUBSCRIPTION_STATUS_UNSPECIFIED, UNRECOGNIZED -> "UNSPECIFIED";
        };
    }

    private static String toHttpIntervalUnit(com.banking.billing.v1.IntervalUnit unit) {
        return switch (unit) {
            case INTERVAL_UNIT_DAY -> "DAY";
            case INTERVAL_UNIT_WEEK -> "WEEK";
            case INTERVAL_UNIT_MONTH -> "MONTH";
            case INTERVAL_UNIT_UNSPECIFIED, UNRECOGNIZED -> "UNSPECIFIED";
        };
    }

    private static com.banking.billing.v1.IntervalUnit parseIntervalUnit(String value) {
        if (value == null) {
            return com.banking.billing.v1.IntervalUnit.INTERVAL_UNIT_UNSPECIFIED;
        }

        return switch (value.trim().toUpperCase()) {
            case "DAY" -> com.banking.billing.v1.IntervalUnit.INTERVAL_UNIT_DAY;
            case "WEEK" -> com.banking.billing.v1.IntervalUnit.INTERVAL_UNIT_WEEK;
            case "MONTH" -> com.banking.billing.v1.IntervalUnit.INTERVAL_UNIT_MONTH;
            default -> com.banking.billing.v1.IntervalUnit.INTERVAL_UNIT_UNSPECIFIED;
        };
    }
}
