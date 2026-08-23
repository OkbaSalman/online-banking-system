package com.banking.cards_service.config;

import com.banking.cards_service.adapter.out.grpc.KycGrpcAdapter;
import com.banking.cards_service.adapter.out.grpc.TransfersGrpcAdapter;
import com.banking.cards_service.adapter.out.jpa.CardChargeJpaAdapter;
import com.banking.cards_service.adapter.out.jpa.CardJpaAdapter;
import com.banking.cards_service.adapter.out.jpa.repository.CardChargeJpaRepository;
import com.banking.cards_service.adapter.out.jpa.repository.CardJpaRepository;
import com.banking.cards_service.application.port.CardChargeQueryPort;
import com.banking.cards_service.application.port.CardChargeRepositoryPort;
import com.banking.cards_service.application.port.CardQueryPort;
import com.banking.cards_service.application.port.CardRepositoryPort;
import com.banking.cards_service.application.port.KycClientPort;
import com.banking.cards_service.application.port.TransfersClientPort;
import com.banking.cards_service.application.usecase.charge_card.ChargeCardService;
import com.banking.cards_service.application.usecase.charge_card.ChargeCardUseCase;
import com.banking.cards_service.application.usecase.create_virtual_card.CreateVirtualCardService;
import com.banking.cards_service.application.usecase.create_virtual_card.CreateVirtualCardUseCase;
import com.banking.cards_service.application.usecase.freeze_card.FreezeCardService;
import com.banking.cards_service.application.usecase.freeze_card.FreezeCardUseCase;
import com.banking.cards_service.application.usecase.get_card.GetCardService;
import com.banking.cards_service.application.usecase.get_card.GetCardUseCase;
import com.banking.cards_service.application.usecase.list_my_cards.ListMyCardsService;
import com.banking.cards_service.application.usecase.list_my_cards.ListMyCardsUseCase;
import com.banking.cards_service.application.usecase.list_my_charges.ListMyChargesService;
import com.banking.cards_service.application.usecase.list_my_charges.ListMyChargesUseCase;
import com.banking.cards_service.application.usecase.set_card_limits.SetCardLimitsService;
import com.banking.cards_service.application.usecase.set_card_limits.SetCardLimitsUseCase;
import com.banking.cards_service.application.usecase.unfreeze_card.UnfreezeCardService;
import com.banking.cards_service.application.usecase.unfreeze_card.UnfreezeCardUseCase;
import com.banking.kyc.v1.KycServiceGrpc;
import com.banking.transfers.v1.TransfersServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CardsUseCaseConfig {

    @Bean
    CardJpaAdapter cardJpaAdapter(CardJpaRepository repo) {
        return new CardJpaAdapter(repo);
    }

    @Bean
    CardChargeJpaAdapter cardChargeJpaAdapter(CardChargeJpaRepository repo) {
        return new CardChargeJpaAdapter(repo);
    }

    @Bean
    TransfersClientPort transfersClientPort(TransfersServiceGrpc.TransfersServiceBlockingStub transfers) {
        return new TransfersGrpcAdapter(transfers);
    }

    @Bean
    KycClientPort kycClientPort(KycServiceGrpc.KycServiceBlockingStub kyc) {
        return new KycGrpcAdapter(kyc);
    }

    @Bean
    CreateVirtualCardUseCase createVirtualCardUseCase(CardRepositoryPort cards, KycClientPort kyc) {
        return new CreateVirtualCardService(cards, kyc);
    }

    @Bean
    GetCardUseCase getCardUseCase(CardRepositoryPort cards) {
        return new GetCardService(cards);
    }

    @Bean
    ListMyCardsUseCase listMyCardsUseCase(CardQueryPort cards) {
        return new ListMyCardsService(cards);
    }

    @Bean
    FreezeCardUseCase freezeCardUseCase(CardRepositoryPort cards, KycClientPort kyc) {
        return new FreezeCardService(cards, kyc);
    }

    @Bean
    UnfreezeCardUseCase unfreezeCardUseCase(CardRepositoryPort cards, KycClientPort kyc) {
        return new UnfreezeCardService(cards, kyc);
    }

    @Bean
    ChargeCardUseCase chargeCardUseCase(
            CardRepositoryPort cards,
            CardChargeRepositoryPort charges,
            CardChargeQueryPort chargeQuery,
            TransfersClientPort transfers,
            KycClientPort kyc
    ) {
        return new ChargeCardService(cards, charges, chargeQuery, transfers, kyc);
    }

    @Bean
    SetCardLimitsUseCase setCardLimitsUseCase(CardRepositoryPort cards) {
        return new SetCardLimitsService(cards);
    }

    @Bean
    ListMyChargesUseCase listMyChargesUseCase(CardRepositoryPort cards, CardChargeQueryPort charges) {
        return new ListMyChargesService(cards, charges);
    }
}
