package com.banking.auth_service.application.usecase.login;

import com.banking.auth_service.application.port.EmailSenderPort;
import com.banking.auth_service.application.port.EmailVerificationCodeRepositoryPort;
import com.banking.auth_service.application.port.JwtIssuerPort;
import com.banking.auth_service.application.port.PasswordHasherPort;
import com.banking.auth_service.application.port.RefreshTokenPort;
import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.application.port.VerificationCodePort;
import com.banking.auth_service.application.usecase.login.dto.LoginCommand;
import com.banking.auth_service.application.usecase.login.dto.LoginResult;


import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
 
import org.springframework.transaction.annotation.Transactional;
 
import com.banking.auth_service.domain.model.User;
import com.banking.auth_service.application.usecase.common.exception.ForbiddenException;

public class LoginService implements LoginUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final JwtIssuerPort jwtIssuer;
    private final RefreshTokenPort refreshTokens;

    private final EmailVerificationCodeRepositoryPort emailCodes;
    private final VerificationCodePort verificationCodes;
    private final EmailSenderPort emailSender;

    public LoginService(
            UserRepositoryPort users,
            PasswordHasherPort passwordHasher,
            JwtIssuerPort jwtIssuer,
            RefreshTokenPort refreshTokens,
            EmailVerificationCodeRepositoryPort emailCodes,
            VerificationCodePort verificationCodes,
            EmailSenderPort emailSender
    ) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.jwtIssuer = jwtIssuer;
        this.refreshTokens = refreshTokens;
        this.emailCodes = emailCodes;
        this.verificationCodes = verificationCodes;
        this.emailSender = emailSender;
    }

    @Transactional
    @Override
    public LoginResult login(LoginCommand command) {
        String email = normalizeEmail(command.email());
        String password = requireNonBlank(command.password(), "Password is required");

        User user = users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.blocked()) {
            throw new ForbiddenException("User is blocked");
        }

        boolean ok = passwordHasher.matches(password, user.passwordHash());
        if (!ok) {
        throw new IllegalArgumentException("Invalid email or password");
        }

    if (!user.emailVerified()) {
        resendVerificationCode(user);
        return new LoginResult(true, "", 0, "", 0);
    }

    var accessTtl = Duration.ofMinutes(15);
    var refreshTtl = Duration.ofDays(7);

    var jwt = jwtIssuer.issueAccessToken(user.id(), user.role(), accessTtl);

    Instant now = Instant.now();
    refreshTokens.revokeAllActiveForUser(user.id(), now);

    String secret = generateSecret();
    String secretHash = passwordHasher.hash(secret);

    Instant refreshExpiresAt = now.plus(refreshTtl);

    var refreshRecord = refreshTokens.create(user.id(), secretHash, refreshExpiresAt);

    String refreshToken = refreshRecord.id().toString() + "." + secret;

    return new LoginResult(
            false,
            jwt.token(),
            jwt.expiresInSeconds(),
            refreshToken,
            refreshTtl.toSeconds()
    );
}


    private void resendVerificationCode(User user) {
    String code = verificationCodes.generate6DigitCode();
    String codeHash = verificationCodes.hash(code);

    Instant expiresAt = Instant.now().plus(Duration.ofMinutes(10));

    emailCodes.createCode(user.id(), codeHash, expiresAt);
    emailSender.sendVerificationCode(user.email(), code);
}

private static String normalizeEmail(String email) {
    return requireNonBlank(email, "Email is required").trim().toLowerCase();
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
}