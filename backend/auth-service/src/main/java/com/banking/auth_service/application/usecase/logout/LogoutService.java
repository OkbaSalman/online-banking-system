package com.banking.auth_service.application.usecase.logout;

import java.time.Instant;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.banking.auth_service.application.port.PasswordHasherPort;
import com.banking.auth_service.application.port.RefreshTokenPort;
import com.banking.auth_service.application.usecase.logout.dto.LogoutCommand;
import com.banking.auth_service.application.usecase.logout.dto.LogoutResult;

public class LogoutService implements LogoutUseCase {

    private final RefreshTokenPort refreshTokens;
    private final PasswordHasherPort passwordHasher;

    public LogoutService(RefreshTokenPort refreshTokens, PasswordHasherPort passwordHasher) {
        this.refreshTokens = refreshTokens;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    @Override
    public LogoutResult logout(LogoutCommand command) {
        String raw = requireNonBlank(command.refreshToken(), "Refresh token is required");

        ParsedRefreshToken parsed = parse(raw);

        var now = Instant.now();

        var record = refreshTokens.findActiveById(parsed.id, now)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        boolean ok = passwordHasher.matches(parsed.secret, record.tokenHash());
        if (!ok) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        refreshTokens.revokeById(record.id(), now);

        return new LogoutResult(true);
    }

    private static ParsedRefreshToken parse(String raw) {
        int dot = raw.indexOf('.');
        if (dot <= 0 || dot == raw.length() - 1) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String idPart = raw.substring(0, dot);
        String secretPart = raw.substring(dot + 1);

        try {
            UUID id = UUID.fromString(idPart);
            if (secretPart.isBlank()) {
                throw new IllegalArgumentException("Invalid refresh token");
            }
            return new ParsedRefreshToken(id, secretPart);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    private record ParsedRefreshToken(UUID id, String secret) {}
}