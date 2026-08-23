package com.banking.ledger_service.config;

import com.banking.ledger_service.application.port.LedgerRepositoryPort;
import com.banking.ledger_service.application.usecase.create_transfer.CreateTransferService;
import com.banking.ledger_service.application.usecase.create_transfer.CreateTransferUseCase;

import com.banking.ledger_service.application.usecase.get_balance.GetBalanceService;
import com.banking.ledger_service.application.usecase.get_balance.GetBalanceUseCase;

import com.banking.ledger_service.application.usecase.get_entry.GetEntryService;
import com.banking.ledger_service.application.usecase.get_entry.GetEntryUseCase;

import com.banking.ledger_service.application.usecase.list_entries.ListEntriesService;
import com.banking.ledger_service.application.usecase.list_entries.ListEntriesUseCase;

import com.banking.ledger_service.application.usecase.get_my_chain_head.GetMyChainHeadService;
import com.banking.ledger_service.application.usecase.get_my_chain_head.GetMyChainHeadUseCase;

import com.banking.ledger_service.application.usecase.verify_my_chain.VerifyMyChainService;
import com.banking.ledger_service.application.usecase.verify_my_chain.VerifyMyChainUseCase;

import com.banking.ledger_service.adapter.out.jpa.LedgerJpaAdapter;
import com.banking.ledger_service.adapter.out.jpa.repository.AccountBalanceJpaRepository;
import com.banking.ledger_service.adapter.out.jpa.repository.AccountLedgerItemJpaRepository;
import com.banking.ledger_service.adapter.out.jpa.repository.LedgerChainHeadJpaRepository;
import com.banking.ledger_service.adapter.out.jpa.repository.LedgerEntryJpaRepository;
import com.banking.ledger_service.adapter.out.jpa.repository.PostingJpaRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LedgerUseCaseConfig {

    @Bean
    LedgerRepositoryPort ledgerRepositoryPort(
            AccountBalanceJpaRepository balances,
            LedgerChainHeadJpaRepository heads,
            LedgerEntryJpaRepository entries,
            PostingJpaRepository postings,
            AccountLedgerItemJpaRepository accountItems
    ) {
        return new LedgerJpaAdapter(balances, heads, entries, postings, accountItems);
    }

    @Bean
    CreateTransferUseCase createTransferUseCase(LedgerRepositoryPort ledger) {
        return new CreateTransferService(ledger);
    }

    @Bean
    GetBalanceUseCase getBalanceUseCase(LedgerRepositoryPort ledger) {
        return new GetBalanceService(ledger);
    }

    @Bean
    GetEntryUseCase getEntryUseCase(LedgerRepositoryPort ledger) {
        return new GetEntryService(ledger);
    }

    @Bean
    ListEntriesUseCase listEntriesUseCase(LedgerRepositoryPort ledger) {
        return new ListEntriesService(ledger);
    }

    @Bean
    GetMyChainHeadUseCase getMyChainHeadUseCase(LedgerRepositoryPort ledger) {
        return new GetMyChainHeadService(ledger);
    }

    @Bean
    VerifyMyChainUseCase verifyMyChainUseCase(LedgerRepositoryPort ledger) {
        return new VerifyMyChainService(ledger);
    }
}