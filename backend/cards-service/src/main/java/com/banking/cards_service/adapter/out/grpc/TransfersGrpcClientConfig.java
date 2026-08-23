package com.banking.cards_service.adapter.out.grpc;

import com.banking.cards_service.adapter.out.grpc.security.AuthMetadataClientInterceptor;
import com.banking.transfers.v1.TransfersServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class TransfersGrpcClientConfig {

    @Bean
    TransfersServiceGrpc.TransfersServiceBlockingStub transfersBlockingStub(GrpcChannelFactory channels) {
        return TransfersServiceGrpc.newBlockingStub(channels.createChannel("transfers"))
                .withInterceptors(new AuthMetadataClientInterceptor());
    }
}
