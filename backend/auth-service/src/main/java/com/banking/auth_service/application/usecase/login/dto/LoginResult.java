package com.banking.auth_service.application.usecase.login.dto;

public record LoginResult(
        boolean verificationRequired,
        String accessToken,
        long accessExpiresInSeconds,
        String refreshToken,
        long refreshExpiresInSeconds
) {}