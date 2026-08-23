package com.banking.transfers_service.grpc;

import com.banking.ledger.v1.LedgerServiceGrpc;
import org.springframework.grpc.client.GrpcChannelFactory;

public class LedgerGrpcClientConfig {

    LedgerServiceGrpc.LedgerServiceBlockingStub ledgerBlockingStub(GrpcChannelFactory channels) {
        return LedgerServiceGrpc.newBlockingStub(channels.createChannel("ledger"));
    }
}
