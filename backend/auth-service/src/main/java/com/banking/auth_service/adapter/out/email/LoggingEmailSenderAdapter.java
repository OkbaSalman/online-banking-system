package com.banking.auth_service.adapter.out.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
 
import com.banking.auth_service.application.port.EmailSenderPort;

@Component
public class LoggingEmailSenderAdapter implements EmailSenderPort {
 
    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSenderAdapter.class);
 
    @Override
    public void sendVerificationCode(String email, String code) {
        log.info("Email verification code for {} is {}", email, code);
    }

    @Override
    public void sendPasswordReset(String email, String token) {
        log.info("Password reset token for {} is {}", email, token);
    }
}
