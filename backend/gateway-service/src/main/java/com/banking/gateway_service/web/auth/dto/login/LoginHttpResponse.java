package com.banking.gateway_service.web.auth.dto.login;

public record LoginHttpResponse(
        boolean verificationRequired,
        String accessToken,
        long accessExpiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds
) {}
