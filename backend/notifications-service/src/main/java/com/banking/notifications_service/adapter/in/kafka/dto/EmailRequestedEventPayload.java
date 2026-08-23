package com.banking.notifications_service.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailRequestedEventPayload(
        String messageId,
        String to,
        String subject,
        String textBody,
        String htmlBody
) {
}
