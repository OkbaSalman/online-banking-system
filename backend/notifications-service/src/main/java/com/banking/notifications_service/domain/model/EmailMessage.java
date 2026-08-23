package com.banking.notifications_service.domain.model;

public record EmailMessage(
        String messageId,
        String to,
        String subject,
        String textBody,
        String htmlBody
) {
}
