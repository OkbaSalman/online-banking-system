package com.banking.auth_service.adapter.out.security;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.banking.auth_service.application.port.VerificationCodePort;

@Component
public class BcryptVerificationCodeAdapter implements VerificationCodePort {
 
    private final SecureRandom random = new SecureRandom();
    private final BcryptPasswordHasherAdapter hasher;
 
    public BcryptVerificationCodeAdapter(BcryptPasswordHasherAdapter hasher) {
        this.hasher = hasher;
    }
 
    @Override
    public String generate6DigitCode() {
        int value = random.nextInt(1_000_000);
        return String.format("%06d", value);
    }
 
    @Override
    public String hash(String code) {
        return hasher.hash(code);
    }
 
    @Override
    public boolean matches(String code, String codeHash) {
        return hasher.matches(code, codeHash);
    }
}