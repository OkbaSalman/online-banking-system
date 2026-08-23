package com.banking.gateway_service.grpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.banking.auth.v1.AuthServiceGrpc;
import com.banking.gateway_service.grpc.security.AuthMetadataClientInterceptor;

@Configuration
public class AuthGrpcClientConfig {
    @Bean
    AuthServiceGrpc.AuthServiceBlockingStub authBlockingStub(GrpcChannelFactory channels) {
        return AuthServiceGrpc.newBlockingStub(channels.createChannel("auth")).withInterceptors(new AuthMetadataClientInterceptor());
    }
}
