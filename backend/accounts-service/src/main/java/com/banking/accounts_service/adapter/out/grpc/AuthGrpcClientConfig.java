package com.banking.accounts_service.adapter.out.grpc;

import com.banking.auth.v1.AuthServiceGrpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class AuthGrpcClientConfig {

    @Bean
    AuthServiceGrpc.AuthServiceBlockingStub authBlockingStub(GrpcChannelFactory channels) {
        return AuthServiceGrpc.newBlockingStub(channels.createChannel("auth"));
    }
}
