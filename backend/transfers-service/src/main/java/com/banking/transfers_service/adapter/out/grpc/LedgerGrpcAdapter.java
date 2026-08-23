package com.banking.transfers_service.adapter.out.grpc;

import com.banking.ledger.v1.CreateTransferRequest;
import com.banking.ledger.v1.GetBalanceRequest;
import com.banking.ledger.v1.GetEntryRequest;
import com.banking.ledger.v1.LedgerEntry;
import com.banking.ledger.v1.LedgerServiceGrpc;
import com.banking.transfers_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import com.banking.transfers_service.application.port.LedgerClientPort;
import com.banking.transfers_service.application.port.LedgerTransferResult;
import io.grpc.Context;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Ledger CreateTransfer is an internal write API gated to ADMIN.
 * Authz already happens in transfers (KYC, canDebit, AML); elevate only for this hop.
 */
public class LedgerGrpcAdapter implements LedgerClientPort {

    private final LedgerServiceGrpc.LedgerServiceBlockingStub ledger;

    public LedgerGrpcAdapter(LedgerServiceGrpc.LedgerServiceBlockingStub ledger) {
        this.ledger = ledger;
    }

    @Override
    public LedgerTransferResult createTransfer(
            UUID initiatorUserId,
            UUID fromAccountId,
            UUID toAccountId,
            long amountCents,
            String idempotencyKey,
            String description
    ) {
        return callAsLedgerWriter(() -> {
            var res = ledger.createTransfer(CreateTransferRequest.newBuilder()
                    .setFromAccountId(fromAccountId.toString())
                    .setToAccountId(toAccountId.toString())
                    .setAmountCents(amountCents)
                    .setIdempotencyKey(idempotencyKey)
                    .setDescription(description == null ? "" : description)
                    .build());

            return new LedgerTransferResult(res.getEntry(), res.getFromBalanceCents(), res.getToBalanceCents());
        });
    }

    @Override
    public LedgerEntry getEntry(UUID entryId) {
        return ledger.getEntry(GetEntryRequest.newBuilder().setEntryId(entryId.toString()).build()).getEntry();
    }

    @Override
    public long getBalanceCents(UUID accountId) {
        return ledger.getBalance(GetBalanceRequest.newBuilder().setAccountId(accountId.toString()).build()).getAvailableCents();
    }

    private static <T> T callAsLedgerWriter(Callable<T> action) {
        UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
        Context ctx = Context.current()
                .withValue(AuthMetadataServerInterceptor.USER_ID_CTX_KEY, userId)
                .withValue(AuthMetadataServerInterceptor.ROLE_CTX_KEY, "ADMIN");
        try {
            return ctx.call(action);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
