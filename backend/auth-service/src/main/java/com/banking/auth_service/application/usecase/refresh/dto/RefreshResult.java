package com.banking.auth_service.application.usecase.refresh.dto;

public record RefreshResult(
        String accessToken,
        long accessExpiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds
) {}