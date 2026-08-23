package com.banking.transfers_service.adapter.out.grpc;

import com.banking.ledger.v1.LedgerServiceGrpc;
import com.banking.transfers_service.adapter.out.grpc.security.AuthMetadataClientInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class LedgerGrpcClientConfig {

    @Bean
    LedgerServiceGrpc.LedgerServiceBlockingStub ledgerBlockingStub(GrpcChannelFactory channels) {
        return LedgerServiceGrpc.newBlockingStub(channels.createChannel("ledger"))
                .withInterceptors(new AuthMetadataClientInterceptor());
    }
}
