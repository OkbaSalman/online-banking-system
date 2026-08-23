package com.banking.ledger_service.config;

import com.banking.ledger_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;

@Configuration
public class GrpcServerConfig {

    @Bean
    @Order(100)
    @GlobalServerInterceptor
    public ServerInterceptor authMetadataServerInterceptor() {
        return new AuthMetadataServerInterceptor();
    }
}