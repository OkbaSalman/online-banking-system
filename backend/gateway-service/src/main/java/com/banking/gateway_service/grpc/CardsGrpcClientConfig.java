package com.banking.gateway_service.grpc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

import com.banking.cards.v1.CardsServiceGrpc;
import com.banking.gateway_service.grpc.security.AuthMetadataClientInterceptor;

@Configuration
public class CardsGrpcClientConfig {
    @Bean
    CardsServiceGrpc.CardsServiceBlockingStub cardsBlockingStub(GrpcChannelFactory channels) {
        return CardsServiceGrpc.newBlockingStub(channels.createChannel("cards"))
                .withInterceptors(new AuthMetadataClientInterceptor());
    }
}
