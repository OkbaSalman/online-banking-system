package com.banking.notifications_service.adapter.in.kafka;

import com.banking.notifications_service.adapter.in.kafka.dto.EmailRequestedEventPayload;
import com.banking.notifications_service.application.usecase.SendEmailUseCase;
import com.banking.notifications_service.domain.model.EmailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmailRequestedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailRequestedKafkaConsumer.class);

    private final ObjectMapper objectMapper;
    private final SendEmailUseCase sendEmailUseCase;

    public EmailRequestedKafkaConsumer(ObjectMapper objectMapper, SendEmailUseCase sendEmailUseCase) {
        this.objectMapper = objectMapper;
        this.sendEmailUseCase = sendEmailUseCase;
    }

    @KafkaListener(
            topics = "notification-email-requested",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(String value) {
        try {
            EmailRequestedEventPayload payload = objectMapper.readValue(value, EmailRequestedEventPayload.class);

            EmailMessage message = new EmailMessage(
                    payload.messageId(),
                    payload.to(),
                    payload.subject(),
                    payload.textBody(),
                    payload.htmlBody()
            );

            sendEmailUseCase.send(message);
        } catch (Exception e) {
            log.warn("Failed to handle notification-email-requested event (will be retried if consumer is configured to retry). payload={}", value, e);
            throw new RuntimeException(e);
        }
    }
}
