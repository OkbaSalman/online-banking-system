package com.banking.cards_service.adapter.in.grpc;

import com.banking.cards.v1.*;
import com.banking.cards_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import com.banking.cards_service.application.usecase.charge_card.ChargeCardUseCase;
import com.banking.cards_service.application.usecase.charge_card.dto.ChargeCardCommand;
import com.banking.cards_service.application.usecase.create_virtual_card.CreateVirtualCardUseCase;
import com.banking.cards_service.application.usecase.create_virtual_card.dto.CreateVirtualCardCommand;
import com.banking.cards_service.application.usecase.freeze_card.FreezeCardUseCase;
import com.banking.cards_service.application.usecase.freeze_card.dto.FreezeCardCommand;
import com.banking.cards_service.application.usecase.get_card.GetCardUseCase;
import com.banking.cards_service.application.usecase.get_card.dto.GetCardQuery;
import com.banking.cards_service.application.usecase.list_my_cards.ListMyCardsUseCase;
import com.banking.cards_service.application.usecase.list_my_cards.dto.ListMyCardsQuery;
import com.banking.cards_service.application.usecase.list_my_charges.ListMyChargesUseCase;
import com.banking.cards_service.application.usecase.list_my_charges.dto.ListMyChargesQuery;
import com.banking.cards_service.application.usecase.set_card_limits.SetCardLimitsUseCase;
import com.banking.cards_service.application.usecase.set_card_limits.dto.SetCardLimitsCommand;
import com.banking.cards_service.application.usecase.unfreeze_card.UnfreezeCardUseCase;
import com.banking.cards_service.application.usecase.unfreeze_card.dto.UnfreezeCardCommand;
import com.banking.cards_service.domain.model.Card;
import com.banking.cards_service.domain.model.CardCharge;
import com.banking.cards_service.domain.model.CardChargeStatus;
import com.banking.cards_service.domain.model.CardStatus;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CardsGrpcController extends CardsServiceGrpc.CardsServiceImplBase {

    private final CreateVirtualCardUseCase createVirtualCardUseCase;
    private final GetCardUseCase getCardUseCase;
    private final ListMyCardsUseCase listMyCardsUseCase;
    private final FreezeCardUseCase freezeCardUseCase;
    private final UnfreezeCardUseCase unfreezeCardUseCase;
    private final ChargeCardUseCase chargeCardUseCase;
    private final ListMyChargesUseCase listMyChargesUseCase;
    private final SetCardLimitsUseCase setCardLimitsUseCase;

    public CardsGrpcController(
            CreateVirtualCardUseCase createVirtualCardUseCase,
            GetCardUseCase getCardUseCase,
            ListMyCardsUseCase listMyCardsUseCase,
            FreezeCardUseCase freezeCardUseCase,
            UnfreezeCardUseCase unfreezeCardUseCase,
            ChargeCardUseCase chargeCardUseCase,
            ListMyChargesUseCase listMyChargesUseCase,
            SetCardLimitsUseCase setCardLimitsUseCase
    ) {
        this.createVirtualCardUseCase = createVirtualCardUseCase;
        this.getCardUseCase = getCardUseCase;
        this.listMyCardsUseCase = listMyCardsUseCase;
        this.freezeCardUseCase = freezeCardUseCase;
        this.unfreezeCardUseCase = unfreezeCardUseCase;
        this.chargeCardUseCase = chargeCardUseCase;
        this.listMyChargesUseCase = listMyChargesUseCase;
        this.setCardLimitsUseCase = setCardLimitsUseCase;
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().setMessage("pong: " + request.getMessage()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void createVirtualCard(CreateVirtualCardRequest request, StreamObserver<CreateVirtualCardResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = createVirtualCardUseCase.create(new CreateVirtualCardCommand(
                    userId,
                    UUID.fromString(request.getFundingAccountId()),
                    request.getIdempotencyKey(),
                    request.getNickname(),
                    request.getDailyLimitCents(),
                    request.getMonthlyLimitCents(),
                    request.getPerTransactionLimitCents()
            ));

            responseObserver.onNext(CreateVirtualCardResponse.newBuilder().setCard(toProto(res.card())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void getCard(GetCardRequest request, StreamObserver<GetCardResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = getCardUseCase.get(new GetCardQuery(userId, UUID.fromString(request.getCardId())));
            responseObserver.onNext(GetCardResponse.newBuilder().setCard(toProto(res.card())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listMyCards(ListMyCardsRequest request, StreamObserver<ListMyCardsResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = listMyCardsUseCase.list(new ListMyCardsQuery(userId, request.getLimit(), request.getOffset()));

            var b = ListMyCardsResponse.newBuilder();
            for (Card c : res.cards()) {
                b.addCards(toProto(c));
            }

            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void freezeCard(FreezeCardRequest request, StreamObserver<FreezeCardResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = freezeCardUseCase.freeze(new FreezeCardCommand(userId, UUID.fromString(request.getCardId())));
            responseObserver.onNext(FreezeCardResponse.newBuilder().setCard(toProto(res.card())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void unfreezeCard(UnfreezeCardRequest request, StreamObserver<UnfreezeCardResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = unfreezeCardUseCase.unfreeze(new UnfreezeCardCommand(userId, UUID.fromString(request.getCardId())));
            responseObserver.onNext(UnfreezeCardResponse.newBuilder().setCard(toProto(res.card())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void setCardLimits(SetCardLimitsRequest request, StreamObserver<SetCardLimitsResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            var res = setCardLimitsUseCase.setLimits(new SetCardLimitsCommand(
                    userId,
                    UUID.fromString(request.getCardId()),
                    request.getDailyLimitCents(),
                    request.getMonthlyLimitCents(),
                    request.getPerTransactionLimitCents()
            ));
            responseObserver.onNext(SetCardLimitsResponse.newBuilder().setCard(toProto(res.card())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void chargeCard(ChargeCardRequest request, StreamObserver<ChargeCardResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = chargeCardUseCase.charge(new ChargeCardCommand(
                    userId,
                    UUID.fromString(request.getCardId()),
                    UUID.fromString(request.getMerchantAccountId()),
                    request.getAmountCents(),
                    request.getIdempotencyKey(),
                    request.getDescription()
            ));

            responseObserver.onNext(ChargeCardResponse.newBuilder().setCharge(toProto(res.charge())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listMyCharges(ListMyChargesRequest request, StreamObserver<ListMyChargesResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            UUID cardId = request.getCardId() == null || request.getCardId().isBlank()
                    ? null
                    : UUID.fromString(request.getCardId());

            var res = listMyChargesUseCase.list(new ListMyChargesQuery(userId, cardId, request.getLimit(), request.getOffset()));

            var b = ListMyChargesResponse.newBuilder();
            for (CardCharge c : res.charges()) {
                b.addCharges(toProto(c));
            }

            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    private static com.banking.cards.v1.Card toProto(Card c) {
        return com.banking.cards.v1.Card.newBuilder()
                .setId(c.id().toString())
                .setUserId(c.userId().toString())
                .setFundingAccountId(c.fundingAccountId().toString())
                .setLast4(c.last4())
                .setStatus(toProtoStatus(c.status()))
                .setCreatedAtEpochMs(c.createdAtEpochMs())
                .setNickname(c.nickname() == null ? "" : c.nickname())
                .setDailyLimitCents(c.dailyLimitCents())
                .setMonthlyLimitCents(c.monthlyLimitCents())
                .setPerTransactionLimitCents(c.perTransactionLimitCents())
                .build();
    }

    private static com.banking.cards.v1.CardCharge toProto(CardCharge c) {
        return com.banking.cards.v1.CardCharge.newBuilder()
                .setId(c.id().toString())
                .setUserId(c.userId().toString())
                .setCardId(c.cardId().toString())
                .setMerchantAccountId(c.merchantAccountId().toString())
                .setAmountCents(c.amountCents())
                .setCreatedAtEpochMs(c.createdAtEpochMs())
                .setStatus(toProtoChargeStatus(c.status()))
                .setIdempotencyKey(c.idempotencyKey())
                .setDescription(c.description() == null ? "" : c.description())
                .setTransferId(c.transferId() == null ? "" : c.transferId().toString())
                .setFailureMessage(c.failureMessage() == null ? "" : c.failureMessage())
                .setFeeCents(c.feeCents())
                .build();
    }

    private static com.banking.cards.v1.CardStatus toProtoStatus(CardStatus status) {
        return switch (status) {
            case ACTIVE -> com.banking.cards.v1.CardStatus.CARD_STATUS_ACTIVE;
            case FROZEN -> com.banking.cards.v1.CardStatus.CARD_STATUS_FROZEN;
            case CLOSED -> com.banking.cards.v1.CardStatus.CARD_STATUS_CLOSED;
        };
    }

    private static com.banking.cards.v1.CardChargeStatus toProtoChargeStatus(CardChargeStatus status) {
        return switch (status) {
            case PENDING -> com.banking.cards.v1.CardChargeStatus.CARD_CHARGE_STATUS_PENDING;
            case COMPLETED -> com.banking.cards.v1.CardChargeStatus.CARD_CHARGE_STATUS_COMPLETED;
            case BLOCKED -> com.banking.cards.v1.CardChargeStatus.CARD_CHARGE_STATUS_BLOCKED;
            case FAILED -> com.banking.cards.v1.CardChargeStatus.CARD_CHARGE_STATUS_FAILED;
        };
    }
}
