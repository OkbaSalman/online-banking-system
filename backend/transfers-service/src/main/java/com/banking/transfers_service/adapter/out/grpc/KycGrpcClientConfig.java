package com.banking.transfers_service.adapter.out.grpc;

import com.banking.kyc.v1.KycServiceGrpc;
import com.banking.transfers_service.adapter.out.grpc.security.AuthMetadataClientInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class KycGrpcClientConfig {

    @Bean
    KycServiceGrpc.KycServiceBlockingStub kycBlockingStub(GrpcChannelFactory channels) {
        return KycServiceGrpc.newBlockingStub(channels.createChannel("kyc"))
                .withInterceptors(new AuthMetadataClientInterceptor());
    }
}
