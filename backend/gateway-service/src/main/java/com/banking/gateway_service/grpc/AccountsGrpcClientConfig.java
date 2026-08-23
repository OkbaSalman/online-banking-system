package com.banking.gateway_service.grpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.banking.accounts.v1.AccountsServiceGrpc;
import com.banking.gateway_service.grpc.security.AuthMetadataClientInterceptor;

@Configuration
public class AccountsGrpcClientConfig {
    @Bean
    AccountsServiceGrpc.AccountsServiceBlockingStub accountsBlockingStub(GrpcChannelFactory channels) {
        return AccountsServiceGrpc.newBlockingStub(channels.createChannel("accounts")).withInterceptors(new AuthMetadataClientInterceptor());
    }
}