package com.banking.auth_service.application.port;

public interface VerificationCodePort {
    String generate6DigitCode();
    String hash(String code);
    boolean matches(String code, String codeHash);
}
