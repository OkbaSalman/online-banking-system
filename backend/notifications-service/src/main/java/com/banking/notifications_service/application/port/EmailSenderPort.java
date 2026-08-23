package com.banking.notifications_service.application.port;

import com.banking.notifications_service.domain.model.EmailMessage;

public interface EmailSenderPort {
    void send(EmailMessage message);
}
