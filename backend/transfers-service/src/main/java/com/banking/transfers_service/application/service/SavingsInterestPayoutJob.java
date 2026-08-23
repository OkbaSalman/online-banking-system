package com.banking.transfers_service.application.service;

import com.banking.transfers_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import com.banking.transfers_service.application.port.AccountType;
import com.banking.transfers_service.application.port.AccountsClientPort;
import com.banking.transfers_service.application.port.LedgerClientPort;
import io.grpc.Context;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class SavingsInterestPayoutJob {

    private final AccountsClientPort accounts;
    private final LedgerClientPort ledger;

    private final boolean enabled;
    private final UUID revenueAccountId;
    private final long monthlyInterestBps;

    public SavingsInterestPayoutJob(
            AccountsClientPort accounts,
            LedgerClientPort ledger,
            @Value("${savings.interest.enabled:false}") boolean enabled,
            @Value("${transfers.revenue-account-id}") String revenueAccountId,
            @Value("${savings.interest.monthly-bps:50}") long monthlyInterestBps
    ) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.enabled = enabled;
        this.revenueAccountId = UUID.fromString(revenueAccountId);
        this.monthlyInterestBps = monthlyInterestBps;
    }

    @Scheduled(cron = "0 0 0 1 * *", zone = "UTC")
    public void payoutMonthlyInterest() {
        if (!enabled) {
            return;
        }

        Context.current()
                .withValue(AuthMetadataServerInterceptor.USER_ID_CTX_KEY, revenueAccountId)
                .withValue(AuthMetadataServerInterceptor.ROLE_CTX_KEY, "ADMIN")
                .run(this::payoutMonthlyInterestInternal);
    }

    private void payoutMonthlyInterestInternal() {

        int limit = 200;
        int offset = 0;

        while (true) {
            var page = accounts.listAccountsByType(AccountType.SAVINGS, limit, offset);
            if (page.isEmpty()) {
                return;
            }

            for (var ref : page) {
                payoutInterestForAccount(ref.accountId());
            }

            offset += page.size();
        }
    }

    private void payoutInterestForAccount(UUID savingsAccountId) {
        long balance = ledger.getBalanceCents(savingsAccountId);
        if (balance <= 0 || monthlyInterestBps <= 0) {
            return;
        }

        long interestCents = calculateInterest(balance, monthlyInterestBps);
        if (interestCents <= 0) {
            return;
        }

        UUID initiatorUserId = revenueAccountId;

        String idempotencyKey = interestIdempotencyKey(savingsAccountId);
        String description = "Monthly savings interest";

        ledger.createTransfer(
                initiatorUserId,
                revenueAccountId,
                savingsAccountId,
                interestCents,
                idempotencyKey,
                description
        );
    }

    private static long calculateInterest(long balanceCents, long monthlyInterestBps) {
        long numerator = balanceCents * monthlyInterestBps;
        return numerator / 10000L;
    }

    private static String interestIdempotencyKey(UUID savingsAccountId) {
        YearMonth ym = YearMonth.from(ZonedDateTime.now(ZoneOffset.UTC));
        return "interest:" + savingsAccountId + ":" + ym;
    }
}
