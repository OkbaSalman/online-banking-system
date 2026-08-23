package com.banking.gateway_service.web.cards;

import com.banking.cards.v1.Card;
import com.banking.cards.v1.CardCharge;
import com.banking.cards.v1.CardsServiceGrpc;
import com.banking.cards.v1.ChargeCardRequest;
import com.banking.cards.v1.CreateVirtualCardRequest;
import com.banking.cards.v1.FreezeCardRequest;
import com.banking.cards.v1.GetCardRequest;
import com.banking.cards.v1.ListMyCardsRequest;
import com.banking.cards.v1.ListMyChargesRequest;
import com.banking.cards.v1.PingRequest;
import com.banking.cards.v1.SetCardLimitsRequest;
import com.banking.cards.v1.UnfreezeCardRequest;
import com.banking.gateway_service.grpc.security.GrpcAuthContext;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
public class CardsController {

    private final CardsServiceGrpc.CardsServiceBlockingStub cards;

    public record PingHttpResponse(String message) {}

    public CardsController(CardsServiceGrpc.CardsServiceBlockingStub cards) {
        this.cards = cards;
    }

    public record CreateCardHttpRequest(
            String fundingAccountId,
            String idempotencyKey,
            String nickname,
            Long dailyLimitCents,
            Long monthlyLimitCents,
            Long perTransactionLimitCents
    ) {}

    public record CardHttpDto(
            String id,
            String userId,
            String fundingAccountId,
            String last4,
            String status,
            long createdAtEpochMs,
            String nickname,
            long dailyLimitCents,
            long monthlyLimitCents,
            long perTransactionLimitCents
    ) {}

    public record CardChargeHttpDto(
            String id,
            String userId,
            String cardId,
            String merchantAccountId,
            long amountCents,
            long feeCents,
            long createdAtEpochMs,
            String status,
            String idempotencyKey,
            String description,
            String transferId,
            String failureMessage
    ) {}

    public record CreateCardHttpResponse(CardHttpDto card) {}

    public record ChargeCardHttpRequest(
            String merchantAccountId,
            long amountCents,
            String idempotencyKey,
            String description
    ) {}

    public record ChargeCardHttpResponse(CardChargeHttpDto charge) {}

    @GetMapping("/api/cards/ping")
    public Mono<PingHttpResponse> ping(Authentication authentication, @RequestParam(defaultValue = "hello") String message) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> cards.ping(
                PingRequest.newBuilder().setMessage(message).build()
        )))
                .subscribeOn(Schedulers.boundedElastic())
                .map(res -> new PingHttpResponse(res.getMessage()));
    }

    @PostMapping("/api/cards")
    public Mono<CreateCardHttpResponse> createCard(Authentication authentication, @RequestBody CreateCardHttpRequest body) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> cards.createVirtualCard(
                CreateVirtualCardRequest.newBuilder()
                        .setFundingAccountId(body.fundingAccountId())
                        .setIdempotencyKey(body.idempotencyKey())
                        .setNickname(body.nickname() == null ? "" : body.nickname())
                        .setDailyLimitCents(body.dailyLimitCents() == null ? 0 : Math.max(body.dailyLimitCents(), 0))
                        .setMonthlyLimitCents(body.monthlyLimitCents() == null ? 0 : Math.max(body.monthlyLimitCents(), 0))
                        .setPerTransactionLimitCents(body.perTransactionLimitCents() == null ? 0 : Math.max(body.perTransactionLimitCents(), 0))
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> new CreateCardHttpResponse(toHttp(res.getCard())));
    }

    @GetMapping("/api/cards/{cardId}")
    public Mono<CardHttpDto> getCard(Authentication authentication, @PathVariable String cardId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> cards.getCard(
                GetCardRequest.newBuilder().setCardId(cardId).build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> toHttp(res.getCard()));
    }

    @GetMapping("/api/cards")
    public Mono<List<CardHttpDto>> listMyCards(
            Authentication authentication,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> cards.listMyCards(
                ListMyCardsRequest.newBuilder().setLimit(limit).setOffset(offset).build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> res.getCardsList().stream().map(CardsController::toHttp).toList());
    }

    @PostMapping("/api/cards/{cardId}/freeze")
    public Mono<CardHttpDto> freeze(Authentication authentication, @PathVariable String cardId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> cards.freezeCard(
                FreezeCardRequest.newBuilder().setCardId(cardId).build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> toHttp(res.getCard()));
    }

    @PostMapping("/api/cards/{cardId}/unfreeze")
    public Mono<CardHttpDto> unfreeze(Authentication authentication, @PathVariable String cardId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> cards.unfreezeCard(
                UnfreezeCardRequest.newBuilder().setCardId(cardId).build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> toHttp(res.getCard()));
    }

    public record SetCardLimitsHttpRequest(
            Long dailyLimitCents,
            Long monthlyLimitCents,
            Long perTransactionLimitCents
    ) {}

    @PatchMapping("/api/cards/{cardId}/limits")
    public Mono<CardHttpDto> setLimits(
            Authentication authentication,
            @PathVariable String cardId,
            @RequestBody SetCardLimitsHttpRequest body
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> cards.setCardLimits(
                SetCardLimitsRequest.newBuilder()
                        .setCardId(cardId)
                        .setDailyLimitCents(body == null || body.dailyLimitCents() == null ? 0 : Math.max(body.dailyLimitCents(), 0))
                        .setMonthlyLimitCents(body == null || body.monthlyLimitCents() == null ? 0 : Math.max(body.monthlyLimitCents(), 0))
                        .setPerTransactionLimitCents(body == null || body.perTransactionLimitCents() == null ? 0 : Math.max(body.perTransactionLimitCents(), 0))
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> toHttp(res.getCard()));
    }

    @PostMapping("/api/cards/{cardId}/charge")
    public Mono<ChargeCardHttpResponse> charge(Authentication authentication, @PathVariable String cardId, @RequestBody ChargeCardHttpRequest body) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> cards.chargeCard(
                ChargeCardRequest.newBuilder()
                        .setCardId(cardId)
                        .setMerchantAccountId(body.merchantAccountId())
                        .setAmountCents(body.amountCents())
                        .setIdempotencyKey(body.idempotencyKey())
                        .setDescription(body.description() == null ? "" : body.description())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> new ChargeCardHttpResponse(toHttp(res.getCharge())));
    }

    @GetMapping("/api/cards/{cardId}/charges")
    public Mono<List<CardChargeHttpDto>> listMyCharges(
            Authentication authentication,
            @PathVariable String cardId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> cards.listMyCharges(
                ListMyChargesRequest.newBuilder().setCardId(cardId).setLimit(limit).setOffset(offset).build()
        ))).subscribeOn(Schedulers.boundedElastic()).map(res -> res.getChargesList().stream().map(CardsController::toHttp).toList());
    }

    private static CardHttpDto toHttp(Card c) {
        return new CardHttpDto(
                c.getId(),
                c.getUserId(),
                c.getFundingAccountId(),
                c.getLast4(),
                toHttpCardStatus(c.getStatus()),
                c.getCreatedAtEpochMs(),
                c.getNickname(),
                c.getDailyLimitCents(),
                c.getMonthlyLimitCents(),
                c.getPerTransactionLimitCents()
        );
    }

    private static CardChargeHttpDto toHttp(CardCharge ch) {
        return new CardChargeHttpDto(
                ch.getId(),
                ch.getUserId(),
                ch.getCardId(),
                ch.getMerchantAccountId(),
                ch.getAmountCents(),
                ch.getFeeCents(),
                ch.getCreatedAtEpochMs(),
                toHttpChargeStatus(ch.getStatus()),
                ch.getIdempotencyKey(),
                ch.getDescription(),
                ch.getTransferId(),
                ch.getFailureMessage()
        );
    }

    private static String toHttpCardStatus(com.banking.cards.v1.CardStatus status) {
        return switch (status) {
            case CARD_STATUS_ACTIVE -> "ACTIVE";
            case CARD_STATUS_FROZEN -> "FROZEN";
            case CARD_STATUS_CLOSED -> "CLOSED";
            case CARD_STATUS_UNSPECIFIED, UNRECOGNIZED -> "UNSPECIFIED";
        };
    }

    private static String toHttpChargeStatus(com.banking.cards.v1.CardChargeStatus status) {
        return switch (status) {
            case CARD_CHARGE_STATUS_PENDING -> "PENDING";
            case CARD_CHARGE_STATUS_COMPLETED -> "COMPLETED";
            case CARD_CHARGE_STATUS_BLOCKED -> "BLOCKED";
            case CARD_CHARGE_STATUS_FAILED -> "FAILED";
            case CARD_CHARGE_STATUS_UNSPECIFIED, UNRECOGNIZED -> "UNSPECIFIED";
        };
    }
}
