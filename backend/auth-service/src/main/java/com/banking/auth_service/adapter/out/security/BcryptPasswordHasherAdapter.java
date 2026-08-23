package com.banking.auth_service.adapter.out.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.banking.auth_service.application.port.PasswordHasherPort;

@Component
public class BcryptPasswordHasherAdapter implements PasswordHasherPort{
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }
 
    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return encoder.matches(rawPassword, passwordHash);
    }
}
