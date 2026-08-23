package com.banking.auth_service.application.usecase.request_password_reset;

import com.banking.auth_service.application.port.EmailSenderPort;
import com.banking.auth_service.application.port.PasswordHasherPort;
import com.banking.auth_service.application.port.PasswordResetTokenPort;
import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.application.usecase.request_password_reset.dto.RequestPasswordResetCommand;
import com.banking.auth_service.application.usecase.request_password_reset.dto.RequestPasswordResetResult;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

public class RequestPasswordResetService implements RequestPasswordResetUseCase {

    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private final UserRepositoryPort users;
    private final PasswordResetTokenPort resetTokens;
    private final PasswordHasherPort passwordHasher;
    private final EmailSenderPort emailSender;

    public RequestPasswordResetService(
            UserRepositoryPort users,
            PasswordResetTokenPort resetTokens,
            PasswordHasherPort passwordHasher,
            EmailSenderPort emailSender
    ) {
        this.users = users;
        this.resetTokens = resetTokens;
        this.passwordHasher = passwordHasher;
        this.emailSender = emailSender;
    }

    @Override
    public RequestPasswordResetResult request(RequestPasswordResetCommand command) {
        String email = normalizeEmail(command.email());

        var userOpt = users.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new RequestPasswordResetResult(true);
        }

        var user = userOpt.get();

        String secret = generateSecret();
        String secretHash = passwordHasher.hash(secret);

        Instant expiresAt = Instant.now().plus(TOKEN_TTL);

        var record = resetTokens.create(user.id(), secretHash, expiresAt);
        String token = record.id().toString() + "." + secret;

        emailSender.sendPasswordReset(email, token);

        return new RequestPasswordResetResult(true);
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }
        String normalized = email.trim().toLowerCase();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        return normalized;
    }

    private static String generateSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
