package com.banking.auth_service.adapter.out.kafka;

import java.util.LinkedHashMap;
import java.util.Map;

import com.banking.auth_service.application.port.EmailSenderPort;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Primary
public class KafkaEmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEmailSenderAdapter.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaEmailSenderAdapter(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${notifications.kafka.email-topic:notification-email-requested}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void sendVerificationCode(String email, String code) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("messageId", "verify:" + email + ":" + System.currentTimeMillis());
            payload.put("to", email);
            payload.put("subject", "Verify your email");
            payload.put("textBody", "Your verification code is: " + code);
            payload.put("htmlBody", "");

            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, email, json);
        } catch (Exception e) {
            log.warn("Failed to publish email verification event (best-effort). to={}", email, e);
        }
    }

    @Override
    public void sendPasswordReset(String email, String token) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("messageId", "pwdreset:" + email + ":" + System.currentTimeMillis());
            payload.put("to", email);
            payload.put("subject", "Reset your password");
            payload.put("textBody", "Use this token to reset your password: " + token);
            payload.put("htmlBody", "");

            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, email, json);
        } catch (Exception e) {
            log.warn("Failed to publish password reset email event (best-effort). to={}", email, e);
        }
    }
}
