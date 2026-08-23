package com.banking.cards_service.adapter.out.grpc;

import com.banking.cards_service.application.port.TransfersClientPort;
import com.banking.transfers.v1.CreateTransferRequest;
import com.banking.transfers.v1.TransfersServiceGrpc;
import com.banking.transfers.v1.Transfer;

import java.util.UUID;

public class TransfersGrpcAdapter implements TransfersClientPort {

    private final TransfersServiceGrpc.TransfersServiceBlockingStub transfers;

    public TransfersGrpcAdapter(TransfersServiceGrpc.TransfersServiceBlockingStub transfers) {
        this.transfers = transfers;
    }

    @Override
    public Transfer createTransfer(UUID fromAccountId, UUID toAccountId, long amountCents, String idempotencyKey, String description) {
        var res = transfers.createTransfer(CreateTransferRequest.newBuilder()
                .setFromAccountId(fromAccountId.toString())
                .setToAccountId(toAccountId.toString())
                .setAmountCents(amountCents)
                .setIdempotencyKey(idempotencyKey)
                .setDescription(description == null ? "" : description)
                .build());

        return res.getTransfer();
    }
}
