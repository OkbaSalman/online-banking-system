package com.banking.auth_service.application.usecase.refresh;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.banking.auth_service.application.port.JwtIssuerPort;
import com.banking.auth_service.application.port.PasswordHasherPort;
import com.banking.auth_service.application.port.RefreshTokenPort;
import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.application.usecase.refresh.dto.RefreshCommand;
import com.banking.auth_service.application.usecase.refresh.dto.RefreshResult;
import com.banking.auth_service.application.usecase.common.exception.ForbiddenException;

public class RefreshService implements RefreshUseCase {

    private final RefreshTokenPort refreshTokens;
    private final PasswordHasherPort passwordHasher;
    private final JwtIssuerPort jwtIssuer;
    private final UserRepositoryPort users;

    public RefreshService(
            RefreshTokenPort refreshTokens,
            PasswordHasherPort passwordHasher,
            JwtIssuerPort jwtIssuer,
            UserRepositoryPort users
    ) {
        this.refreshTokens = refreshTokens;
        this.passwordHasher = passwordHasher;
        this.jwtIssuer = jwtIssuer;
        this.users = users;
    }

    @Transactional
    @Override
    public RefreshResult refresh(RefreshCommand command) {
        String raw = requireNonBlank(command.refreshToken(), "Refresh token is required");

        ParsedRefreshToken parsed = parse(raw);

        Instant now = Instant.now();

        var record = refreshTokens.findActiveById(parsed.id, now)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        boolean ok = passwordHasher.matches(parsed.secret, record.tokenHash());
        if (!ok) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        refreshTokens.revokeById(record.id(), now);

        var user = users.findById(record.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.blocked()) {
            throw new ForbiddenException("User is blocked");
        }

        var accessTtl = Duration.ofMinutes(15);
        var refreshTtl = Duration.ofDays(7);

        var jwt = jwtIssuer.issueAccessToken(user.id(), user.role(), accessTtl);

        String newSecret = generateSecret();
        String newSecretHash = passwordHasher.hash(newSecret);
        Instant newRefreshExpiresAt = now.plus(refreshTtl);

        var newRefreshRecord = refreshTokens.create(user.id(), newSecretHash, newRefreshExpiresAt);
        String newRefreshToken = newRefreshRecord.id().toString() + "." + newSecret;

        return new RefreshResult(
                jwt.token(),
                jwt.expiresInSeconds(),
                newRefreshToken,
                refreshTtl.toSeconds()
        );
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

    private static String generateSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private record ParsedRefreshToken(UUID id, String secret) {}
}