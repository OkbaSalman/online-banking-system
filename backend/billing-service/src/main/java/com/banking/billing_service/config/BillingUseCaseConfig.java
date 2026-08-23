package com.banking.billing_service.config;

import com.banking.kyc.v1.KycServiceGrpc;
import com.banking.transfers.v1.TransfersServiceGrpc;
import com.banking.billing_service.adapter.out.grpc.KycGrpcAdapter;
import com.banking.billing_service.adapter.out.grpc.TransfersGrpcAdapter;
import com.banking.billing_service.adapter.out.jpa.BillingPaymentJpaAdapter;
import com.banking.billing_service.adapter.out.jpa.SubscriptionJpaAdapter;
import com.banking.billing_service.adapter.out.jpa.repository.BillingPaymentJpaRepository;
import com.banking.billing_service.adapter.out.jpa.repository.SubscriptionJpaRepository;
import com.banking.billing_service.application.port.BillingPaymentQueryPort;
import com.banking.billing_service.application.port.BillingPaymentRepositoryPort;
import com.banking.billing_service.application.port.KycClientPort;
import com.banking.billing_service.application.port.SubscriptionQueryPort;
import com.banking.billing_service.application.port.SubscriptionRepositoryPort;
import com.banking.billing_service.application.port.TransfersClientPort;
import com.banking.billing_service.application.usecase.cancel_subscription.CancelSubscriptionService;
import com.banking.billing_service.application.usecase.cancel_subscription.CancelSubscriptionUseCase;
import com.banking.billing_service.application.usecase.create_subscription.CreateSubscriptionService;
import com.banking.billing_service.application.usecase.create_subscription.CreateSubscriptionUseCase;
import com.banking.billing_service.application.usecase.get_subscription.GetSubscriptionService;
import com.banking.billing_service.application.usecase.get_subscription.GetSubscriptionUseCase;
import com.banking.billing_service.application.usecase.list_my_payments.ListMyPaymentsService;
import com.banking.billing_service.application.usecase.list_my_payments.ListMyPaymentsUseCase;
import com.banking.billing_service.application.usecase.list_my_subscriptions.ListMySubscriptionsService;
import com.banking.billing_service.application.usecase.list_my_subscriptions.ListMySubscriptionsUseCase;
import com.banking.billing_service.application.usecase.pay_bill.PayBillService;
import com.banking.billing_service.application.usecase.pay_bill.PayBillUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BillingUseCaseConfig {

    @Bean
    SubscriptionJpaAdapter subscriptionJpaAdapter(SubscriptionJpaRepository repo) {
        return new SubscriptionJpaAdapter(repo);
    }

    @Bean
    BillingPaymentJpaAdapter billingPaymentJpaAdapter(BillingPaymentJpaRepository repo) {
        return new BillingPaymentJpaAdapter(repo);
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
    PayBillUseCase payBillUseCase(BillingPaymentRepositoryPort payments, TransfersClientPort transfers, KycClientPort kyc) {
        return new PayBillService(payments, transfers, kyc);
    }

    @Bean
    CreateSubscriptionUseCase createSubscriptionUseCase(
            SubscriptionRepositoryPort subscriptions,
            KycClientPort kyc,
            PayBillUseCase payBill
    ) {
        return new CreateSubscriptionService(subscriptions, kyc, payBill);
    }

    @Bean
    CancelSubscriptionUseCase cancelSubscriptionUseCase(SubscriptionRepositoryPort subscriptions) {
        return new CancelSubscriptionService(subscriptions);
    }

    @Bean
    GetSubscriptionUseCase getSubscriptionUseCase(SubscriptionRepositoryPort subscriptions) {
        return new GetSubscriptionService(subscriptions);
    }

    @Bean
    ListMySubscriptionsUseCase listMySubscriptionsUseCase(SubscriptionQueryPort subscriptions) {
        return new ListMySubscriptionsService(subscriptions);
    }

    @Bean
    ListMyPaymentsUseCase listMyPaymentsUseCase(BillingPaymentQueryPort payments) {
        return new ListMyPaymentsService(payments);
    }
}
