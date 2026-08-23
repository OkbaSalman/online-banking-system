package com.banking.gateway_service.web.auth.dto.refresh;

public record RefreshHttpResponse(
        String accessToken,
        long accessExpiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds
) {}
