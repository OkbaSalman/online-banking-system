package com.banking.auth_service.application.usecase.resend_verification_code;

import com.banking.auth_service.application.port.EmailSenderPort;
import com.banking.auth_service.application.port.EmailVerificationCodeRepositoryPort;
import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.application.port.VerificationCodePort;
import com.banking.auth_service.application.usecase.resend_verification_code.dto.ResendVerificationCodeCommand;
import com.banking.auth_service.application.usecase.resend_verification_code.dto.ResendVerificationCodeResult;

import java.time.Duration;
import java.time.Instant;

public class ResendVerificationCodeService implements ResendVerificationCodeUseCase {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);

    private final UserRepositoryPort users;
    private final EmailVerificationCodeRepositoryPort emailCodes;
    private final VerificationCodePort verificationCodes;
    private final EmailSenderPort emailSender;

    public ResendVerificationCodeService(
            UserRepositoryPort users,
            EmailVerificationCodeRepositoryPort emailCodes,
            VerificationCodePort verificationCodes,
            EmailSenderPort emailSender
    ) {
        this.users = users;
        this.emailCodes = emailCodes;
        this.verificationCodes = verificationCodes;
        this.emailSender = emailSender;
    }

    @Override
    public ResendVerificationCodeResult resend(ResendVerificationCodeCommand command) {
        String email = normalizeEmail(command.email());

        var userOpt = users.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new ResendVerificationCodeResult(false);
        }

        var user = userOpt.get();
        if (user.emailVerified()) {
            return new ResendVerificationCodeResult(false);
        }

        String code = verificationCodes.generate6DigitCode();
        String codeHash = verificationCodes.hash(code);

        Instant expiresAt = Instant.now().plus(CODE_TTL);
        emailCodes.createCode(user.id(), codeHash, expiresAt);

        emailSender.sendVerificationCode(email, code);

        return new ResendVerificationCodeResult(true);
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
