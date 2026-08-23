package com.banking.notifications_service.application.usecase;

import com.banking.notifications_service.application.port.EmailSenderPort;
import com.banking.notifications_service.domain.model.EmailMessage;

import org.springframework.stereotype.Component;

@Component
public class SendEmailUseCase {

    private final EmailSenderPort emailSender;

    public SendEmailUseCase(EmailSenderPort emailSender) {
        this.emailSender = emailSender;
    }

    public void send(EmailMessage message) {
        emailSender.send(message);
    }
}
