package com.banking.gateway_service.grpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.banking.billing.v1.BillingServiceGrpc;
import com.banking.gateway_service.grpc.security.AuthMetadataClientInterceptor;

@Configuration
public class BillingGrpcClientConfig {
    @Bean
    BillingServiceGrpc.BillingServiceBlockingStub billingBlockingStub(GrpcChannelFactory channels) {
        return BillingServiceGrpc.newBlockingStub(channels.createChannel("billing"))
                .withInterceptors(new AuthMetadataClientInterceptor());
    }
}
