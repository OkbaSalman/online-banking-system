package com.banking.kyc_service.config;

import com.banking.kyc_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.grpc.server.GlobalServerInterceptor;

@Configuration
public class GrpcServerConfig {
    private static final Logger log = LoggerFactory.getLogger(GrpcServerConfig.class);

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Order(100)
    @GlobalServerInterceptor
    public ServerInterceptor authMetadataServerInterceptor() {
        log.info("Registering AuthMetadataServerInterceptor as global interceptor");
        return new AuthMetadataServerInterceptor();
    }
}