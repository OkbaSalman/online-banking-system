package com.banking.gateway_service.grpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.banking.gateway_service.grpc.security.AuthMetadataClientInterceptor;
import com.banking.kyc.v1.KycServiceGrpc;

@Configuration
public class KycGrpcClientConfig {
    @Bean
    KycServiceGrpc.KycServiceBlockingStub kycBlockingStub(GrpcChannelFactory channels) {
        return KycServiceGrpc.newBlockingStub(channels.createChannel("kyc")).withInterceptors(new AuthMetadataClientInterceptor());
    }
}
