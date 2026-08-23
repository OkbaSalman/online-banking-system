package com.banking.auth_service.application.usecase.register;

import java.time.Instant;
import java.util.UUID;
import java.time.Duration;

import com.banking.auth_service.application.port.EmailSenderPort;
import com.banking.auth_service.application.port.EmailVerificationCodeRepositoryPort;
import com.banking.auth_service.application.port.PasswordHasherPort;
import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.application.port.VerificationCodePort;
import com.banking.auth_service.application.usecase.register.dto.RegisterUserCommand;
import com.banking.auth_service.application.usecase.register.dto.RegisterUserResult;
import com.banking.auth_service.domain.model.Role;
import com.banking.auth_service.domain.model.User;

public class RegisterUserService implements RegisterUserUseCase {
        private static final Duration CODE_TTL = Duration.ofMinutes(10);
 
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final VerificationCodePort verificationCodes;
    private final EmailVerificationCodeRepositoryPort emailCodes;
    private final EmailSenderPort emailSender;
 
    public RegisterUserService(
            UserRepositoryPort users,
            PasswordHasherPort passwordHasher,
            VerificationCodePort verificationCodes,
            EmailVerificationCodeRepositoryPort emailCodes,
            EmailSenderPort emailSender
    ) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.verificationCodes = verificationCodes;
        this.emailCodes = emailCodes;
        this.emailSender = emailSender;
    }
 
    @Override
    public RegisterUserResult register(RegisterUserCommand command) {
        String email = normalizeEmail(command.email());
 
        users.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("Email already registered");
        });
 
        UUID userId = UUID.randomUUID();
        String passwordHash = passwordHasher.hash(command.password());
 
        User user = new User(userId, email, passwordHash, Role.CUSTOMER, false, false);
        users.save(user);
 
        String code = verificationCodes.generate6DigitCode();
        String codeHash = verificationCodes.hash(code);
 
        Instant expiresAt = Instant.now().plus(CODE_TTL);
        emailCodes.createCode(userId, codeHash, expiresAt);
 
        emailSender.sendVerificationCode(email, code);
 
        return new RegisterUserResult(userId, true);
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
}
