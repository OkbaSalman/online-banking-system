package com.banking.gateway_service.grpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.banking.gateway_service.grpc.security.AuthMetadataClientInterceptor;
import com.banking.transfers.v1.TransfersServiceGrpc;

@Configuration
public class TransfersGrpcClientConfig {
    @Bean
    TransfersServiceGrpc.TransfersServiceBlockingStub transfersBlockingStub(GrpcChannelFactory channels) {
        return TransfersServiceGrpc.newBlockingStub(channels.createChannel("transfers")).withInterceptors(new AuthMetadataClientInterceptor());
    }
}
