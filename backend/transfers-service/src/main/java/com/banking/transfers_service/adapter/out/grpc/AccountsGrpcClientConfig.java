package com.banking.transfers_service.adapter.out.grpc;

import com.banking.accounts.v1.AccountsServiceGrpc;
import com.banking.transfers_service.adapter.out.grpc.security.AuthMetadataClientInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class AccountsGrpcClientConfig {

    @Bean
    AccountsServiceGrpc.AccountsServiceBlockingStub accountsBlockingStub(GrpcChannelFactory channels) {
        return AccountsServiceGrpc.newBlockingStub(channels.createChannel("accounts"))
                .withInterceptors(new AuthMetadataClientInterceptor());
    }
}
