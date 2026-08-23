package com.banking.auth_service.application.port;

public interface EmailSenderPort {
    void sendVerificationCode(String email, String code);

    void sendPasswordReset(String email, String token);
}
