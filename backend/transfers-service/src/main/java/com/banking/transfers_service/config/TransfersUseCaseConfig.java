package com.banking.transfers_service.config;

import com.banking.accounts.v1.AccountsServiceGrpc;
import com.banking.ledger.v1.LedgerServiceGrpc;
import com.banking.kyc.v1.KycServiceGrpc;
import com.banking.transfers_service.adapter.out.aml.RuleBasedAmlAdapter;
import com.banking.transfers_service.adapter.out.grpc.AccountsGrpcAdapter;
import com.banking.transfers_service.adapter.out.grpc.KycGrpcAdapter;
import com.banking.transfers_service.adapter.out.grpc.LedgerGrpcAdapter;
import com.banking.transfers_service.adapter.out.jpa.TransferJpaAdapter;
import com.banking.transfers_service.adapter.out.jpa.repository.TransferJpaRepository;
import com.banking.transfers_service.application.port.AccountsClientPort;
import com.banking.transfers_service.application.port.AmlPort;
import com.banking.transfers_service.application.port.KycClientPort;
import com.banking.transfers_service.application.port.LedgerClientPort;
import com.banking.transfers_service.application.port.TransferQueryPort;
import com.banking.transfers_service.application.port.TransferRepositoryPort;
import com.banking.transfers_service.application.usecase.admin_get_revenue_summary.AdminGetRevenueSummaryService;
import com.banking.transfers_service.application.usecase.admin_get_revenue_summary.AdminGetRevenueSummaryUseCase;
import com.banking.transfers_service.application.usecase.admin_mint.AdminMintService;
import com.banking.transfers_service.application.usecase.admin_mint.AdminMintUseCase;
import com.banking.transfers_service.application.usecase.admin_list_transfers.AdminListTransfersService;
import com.banking.transfers_service.application.usecase.admin_list_transfers.AdminListTransfersUseCase;
import com.banking.transfers_service.application.usecase.create_transfer.CreateTransferService;
import com.banking.transfers_service.application.usecase.create_transfer.CreateTransferUseCase;
import com.banking.transfers_service.application.usecase.get_transfer.GetTransferService;
import com.banking.transfers_service.application.usecase.get_transfer.GetTransferUseCase;
import com.banking.transfers_service.application.usecase.list_my_transfers.ListMyTransfersService;
import com.banking.transfers_service.application.usecase.list_my_transfers.ListMyTransfersUseCase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class TransfersUseCaseConfig {

    @Bean
    TransferJpaAdapter transferJpaAdapter(TransferJpaRepository repo) {
        return new TransferJpaAdapter(repo);
    }

    @Bean
    LedgerClientPort ledgerClientPort(LedgerServiceGrpc.LedgerServiceBlockingStub ledger) {
        return new LedgerGrpcAdapter(ledger);
    }

    @Bean
    AccountsClientPort accountsClientPort(AccountsServiceGrpc.AccountsServiceBlockingStub accounts) {
        return new AccountsGrpcAdapter(accounts);
    }

    @Bean
    KycClientPort kycClientPort(KycServiceGrpc.KycServiceBlockingStub kyc) {
        return new KycGrpcAdapter(kyc);
    }

    @Bean
    AmlPort amlPort(
            TransferQueryPort transfers,
            @Value("${aml.max-single-transfer-cents:100000000}") long maxSingleTransferCents,
            @Value("${aml.velocity.window-ms:600000}") long velocityWindowMs,
            @Value("${aml.velocity.max-count:20}") long velocityMaxCount
    ) {
        return new RuleBasedAmlAdapter(transfers, maxSingleTransferCents, velocityWindowMs, velocityMaxCount);
    }

    @Bean
    CreateTransferUseCase createTransferUseCase(
            TransferRepositoryPort transfers,
            LedgerClientPort ledger,
            AmlPort aml,
            TransferQueryPort transferQueryPort,
            AccountsClientPort accountsClientPort,
            KycClientPort kycClientPort,
            @Value("${transfers.revenue-account-id}") String revenueAccountId,
            @Value("${transfers.fee.bps:25}") long feeBps,
            @Value("${transfers.savings.max-debits-per-month:10}") long savingsMaxDebitsPerMonth
    ) {
        return new CreateTransferService(
                transfers,
                ledger,
                aml,
                transferQueryPort,
                accountsClientPort,
                kycClientPort,
                UUID.fromString(revenueAccountId),
                feeBps,
                savingsMaxDebitsPerMonth
        );
    }

    @Bean
    GetTransferUseCase getTransferUseCase(TransferRepositoryPort transfers) {
        return new GetTransferService(transfers);
    }

    @Bean
    ListMyTransfersUseCase listMyTransfersUseCase(TransferRepositoryPort transfers, AccountsClientPort accountsClientPort) {
        return new ListMyTransfersService(transfers, accountsClientPort);
    }

    @Bean
    AdminListTransfersUseCase adminListTransfersUseCase(TransferRepositoryPort transfers) {
        return new AdminListTransfersService(transfers);
    }

    @Bean
    AdminGetRevenueSummaryUseCase adminGetRevenueSummaryUseCase(TransferQueryPort transfers) {
        return new AdminGetRevenueSummaryService(transfers);
    }

    @Bean
    AdminMintUseCase adminMintUseCase(
            TransferRepositoryPort transfers,
            LedgerClientPort ledger,
            @Value("${transfers.treasury-account-id}") String treasuryAccountId
    ) {
        return new AdminMintService(transfers, ledger, UUID.fromString(treasuryAccountId));
    }
}
