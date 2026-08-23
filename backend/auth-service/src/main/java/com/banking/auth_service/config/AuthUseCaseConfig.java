package com.banking.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.banking.auth_service.application.port.EmailSenderPort;
import com.banking.auth_service.application.port.EmailVerificationCodeRepositoryPort;
import com.banking.auth_service.application.port.PasswordHasherPort;
import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.application.port.VerificationCodePort;
import com.banking.auth_service.application.usecase.register.RegisterUserService;
import com.banking.auth_service.application.usecase.register.RegisterUserUseCase;
import com.banking.auth_service.application.usecase.verify_email.VerifyEmailService;
import com.banking.auth_service.application.usecase.verify_email.VerifyEmailUseCase;

import com.banking.auth_service.application.port.JwtIssuerPort;
import com.banking.auth_service.application.port.RefreshTokenPort;
import com.banking.auth_service.application.usecase.login.LoginService;
import com.banking.auth_service.application.usecase.login.LoginUseCase;

import com.banking.auth_service.application.usecase.refresh.RefreshService;
import com.banking.auth_service.application.usecase.refresh.RefreshUseCase;

import com.banking.auth_service.application.usecase.logout.LogoutService;
import com.banking.auth_service.application.usecase.logout.LogoutUseCase;

import com.banking.auth_service.application.port.PasswordResetTokenPort;
import com.banking.auth_service.application.usecase.request_password_reset.RequestPasswordResetService;
import com.banking.auth_service.application.usecase.request_password_reset.RequestPasswordResetUseCase;
import com.banking.auth_service.application.usecase.reset_password.ResetPasswordService;
import com.banking.auth_service.application.usecase.reset_password.ResetPasswordUseCase;
import com.banking.auth_service.application.usecase.resend_verification_code.ResendVerificationCodeService;
import com.banking.auth_service.application.usecase.resend_verification_code.ResendVerificationCodeUseCase;
import com.banking.auth_service.application.usecase.search_users.SearchUsersService;
import com.banking.auth_service.application.usecase.search_users.SearchUsersUseCase;
import com.banking.auth_service.application.usecase.set_user_blocked.SetUserBlockedService;
import com.banking.auth_service.application.usecase.set_user_blocked.SetUserBlockedUseCase;

@Configuration
public class AuthUseCaseConfig {
    @Bean
    RegisterUserUseCase registerUserUseCase(
            UserRepositoryPort users,
            PasswordHasherPort passwordHasher,
            VerificationCodePort verificationCodes,
            EmailVerificationCodeRepositoryPort emailCodes,
            EmailSenderPort emailSender
    ) {
        return new RegisterUserService(users, passwordHasher, verificationCodes, emailCodes, emailSender);
    }

    @Bean
    VerifyEmailUseCase verifyEmailUseCase(
        UserRepositoryPort users,
        EmailVerificationCodeRepositoryPort emailCodes,
        VerificationCodePort verificationCodes
    ) {
    return new VerifyEmailService(users, emailCodes, verificationCodes);
    }

    @Bean
    LoginUseCase loginUseCase(
        UserRepositoryPort users,
        PasswordHasherPort passwordHasher,
        JwtIssuerPort jwtIssuer,
        RefreshTokenPort refreshTokens,
        EmailVerificationCodeRepositoryPort emailCodes,
        VerificationCodePort verificationCodes,
        EmailSenderPort emailSender
    ) {
    return new LoginService(
            users,
            passwordHasher,
            jwtIssuer,
            refreshTokens,
            emailCodes,
            verificationCodes,
            emailSender
        );
    }

    @Bean
    RefreshUseCase refreshUseCase(
        RefreshTokenPort refreshTokens,
        PasswordHasherPort passwordHasher,
        JwtIssuerPort jwtIssuer,
        UserRepositoryPort users
    ) {
    return new RefreshService(refreshTokens, passwordHasher, jwtIssuer, users);
    }

    @Bean
    LogoutUseCase logoutUseCase(
        RefreshTokenPort refreshTokens,
        PasswordHasherPort passwordHasher
    ) {
    return new LogoutService(refreshTokens, passwordHasher);
    }

    @Bean
    ResendVerificationCodeUseCase resendVerificationCodeUseCase(
            UserRepositoryPort users,
            EmailVerificationCodeRepositoryPort emailCodes,
            VerificationCodePort verificationCodes,
            EmailSenderPort emailSender
    ) {
        return new ResendVerificationCodeService(users, emailCodes, verificationCodes, emailSender);
    }

    @Bean
    RequestPasswordResetUseCase requestPasswordResetUseCase(
            UserRepositoryPort users,
            PasswordResetTokenPort resetTokens,
            PasswordHasherPort passwordHasher,
            EmailSenderPort emailSender
    ) {
        return new RequestPasswordResetService(users, resetTokens, passwordHasher, emailSender);
    }

    @Bean
    ResetPasswordUseCase resetPasswordUseCase(
            UserRepositoryPort users,
            PasswordResetTokenPort resetTokens,
            PasswordHasherPort passwordHasher,
            RefreshTokenPort refreshTokens
    ) {
        return new ResetPasswordService(users, resetTokens, passwordHasher, refreshTokens);
    }

    @Bean
    SearchUsersUseCase searchUsersUseCase(UserRepositoryPort users) {
        return new SearchUsersService(users);
    }

    @Bean
    SetUserBlockedUseCase setUserBlockedUseCase(UserRepositoryPort users) {
        return new SetUserBlockedService(users);
    }

   
}
