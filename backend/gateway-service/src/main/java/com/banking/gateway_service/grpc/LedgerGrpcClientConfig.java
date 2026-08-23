package com.banking.gateway_service.grpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.banking.gateway_service.grpc.security.AuthMetadataClientInterceptor;
import com.banking.ledger.v1.LedgerServiceGrpc;

@Configuration
public class LedgerGrpcClientConfig {
    @Bean
    LedgerServiceGrpc.LedgerServiceBlockingStub ledgerBlockingStub(GrpcChannelFactory channels) {
        return LedgerServiceGrpc.newBlockingStub(channels.createChannel("ledger")).withInterceptors(new AuthMetadataClientInterceptor());
    }
}
