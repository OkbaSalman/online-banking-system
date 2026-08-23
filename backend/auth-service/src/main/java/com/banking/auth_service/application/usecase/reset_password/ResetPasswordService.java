package com.banking.auth_service.application.usecase.reset_password;

import com.banking.auth_service.application.port.PasswordHasherPort;
import com.banking.auth_service.application.port.PasswordResetTokenPort;
import com.banking.auth_service.application.port.RefreshTokenPort;
import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.application.usecase.reset_password.dto.ResetPasswordCommand;
import com.banking.auth_service.application.usecase.reset_password.dto.ResetPasswordResult;

import java.time.Instant;
import java.util.UUID;

public class ResetPasswordService implements ResetPasswordUseCase {

    private final UserRepositoryPort users;
    private final PasswordResetTokenPort resetTokens;
    private final PasswordHasherPort passwordHasher;
    private final RefreshTokenPort refreshTokens;

    public ResetPasswordService(
            UserRepositoryPort users,
            PasswordResetTokenPort resetTokens,
            PasswordHasherPort passwordHasher,
            RefreshTokenPort refreshTokens
    ) {
        this.users = users;
        this.resetTokens = resetTokens;
        this.passwordHasher = passwordHasher;
        this.refreshTokens = refreshTokens;
    }

    @Override
    public ResetPasswordResult reset(ResetPasswordCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command is required");
        }

        String token = requireNonBlank(command.token(), "token is required");
        String newPassword = requireNonBlank(command.newPassword(), "new_password is required");

        String[] parts = token.split("\\.", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid token");
        }

        UUID tokenId;
        try {
            tokenId = UUID.fromString(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid token");
        }

        String secret = parts[1];
        Instant now = Instant.now();

        var record = resetTokens.findActiveById(tokenId, now)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        boolean ok = passwordHasher.matches(secret, record.tokenHash());
        if (!ok) {
            throw new IllegalArgumentException("Invalid token");
        }

        var user = users.findById(record.userId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        String newPasswordHash = passwordHasher.hash(newPassword);

        var updated = new com.banking.auth_service.domain.model.User(
                user.id(),
                user.email(),
                newPasswordHash,
                user.role(),
                user.emailVerified(),
                user.blocked()
        );
        users.save(updated);

        resetTokens.consume(record.id(), now);
        refreshTokens.revokeAllActiveForUser(user.id(), now);

        return new ResetPasswordResult(true);
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
}
