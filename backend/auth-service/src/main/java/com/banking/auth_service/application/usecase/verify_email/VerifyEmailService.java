package com.banking.auth_service.application.usecase.verify_email;

import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;

import com.banking.auth_service.application.port.EmailVerificationCodeRepositoryPort;
import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.application.port.VerificationCodePort;
import com.banking.auth_service.application.usecase.verify_email.dto.VerifyEmailCommand;
import com.banking.auth_service.application.usecase.verify_email.dto.VerifyEmailResult;
import com.banking.auth_service.domain.model.User;

public class VerifyEmailService implements VerifyEmailUseCase {

    private final UserRepositoryPort users;
    private final EmailVerificationCodeRepositoryPort emailCodes;
    private final VerificationCodePort verificationCodes;

    public VerifyEmailService(
            UserRepositoryPort users,
            EmailVerificationCodeRepositoryPort emailCodes,
            VerificationCodePort verificationCodes
    ) {
        this.users = users;
        this.emailCodes = emailCodes;
        this.verificationCodes = verificationCodes;
    }

    @Transactional
    @Override
    public VerifyEmailResult verify(VerifyEmailCommand command) {
        String email = normalizeEmail(command.email());
        String code = normalizeCode(command.code());

        User user = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or code"));

        if (user.emailVerified()) {
            return new VerifyEmailResult(true);
        }

        var now = Instant.now();

        var codeRecord = emailCodes.findLatestActiveCode(user.id(), now)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or code"));

        boolean ok = verificationCodes.matches(code, codeRecord.codeHash());
        if (!ok) {
            throw new IllegalArgumentException("Invalid email or code");
        }

        emailCodes.consume(codeRecord.id(), now);

        User verifiedUser = new User(
                user.id(),
                user.email(),
                user.passwordHash(),
                user.role(),
                true,
                user.blocked()
        );
        users.save(verifiedUser);

        return new VerifyEmailResult(true);
    }

    private static String normalizeEmail(String email) {
        if (email == null) throw new IllegalArgumentException("Email is required");
        String normalized = email.trim().toLowerCase();
        if (normalized.isBlank()) throw new IllegalArgumentException("Email is required");
        return normalized;
    }

    private static String normalizeCode(String code) {
        if (code == null) throw new IllegalArgumentException("Code is required");
        String normalized = code.trim();
        if (normalized.isBlank()) throw new IllegalArgumentException("Code is required");
        return normalized;
    }
}