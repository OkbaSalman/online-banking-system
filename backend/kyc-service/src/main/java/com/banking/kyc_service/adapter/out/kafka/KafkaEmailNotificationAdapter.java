package com.banking.kyc_service.adapter.out.kafka;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.banking.auth.v1.AuthServiceGrpc;
import com.banking.auth.v1.GetUserRequest;
import com.banking.kyc_service.application.port.EmailNotificationPort;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEmailNotificationAdapter implements EmailNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaEmailNotificationAdapter.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AuthServiceGrpc.AuthServiceBlockingStub auth;
    private final String emailTopic;

    public KafkaEmailNotificationAdapter(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            AuthServiceGrpc.AuthServiceBlockingStub auth,
            @Value("${notifications.kafka.email-topic:notification-email-requested}") String emailTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.auth = auth;
        this.emailTopic = emailTopic;
    }

    @Override
    public void sendKycStatusChanged(UUID userId, String status, String rejectionReason) {
        try {
            var user = auth.getUser(GetUserRequest.newBuilder().setUserId(userId.toString()).build());
            String email = user.getEmail();
            if (email == null || email.isBlank()) {
                log.warn("Cannot send KYC email: empty email. userId={}, status={}", userId, status);
                return;
            }

            String subject = "KYC status update";
            String textBody;

            if ("APPROVED".equalsIgnoreCase(status)) {
                textBody = "Your KYC application has been approved.";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                String reason = rejectionReason == null ? "" : rejectionReason;
                textBody = reason.isBlank()
                        ? "Your KYC application has been rejected."
                        : "Your KYC application has been rejected. Reason: " + reason;
            } else {
                textBody = "Your KYC application status changed: " + status;
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("messageId", "kyc:" + userId + ":" + System.currentTimeMillis());
            payload.put("to", email);
            payload.put("subject", subject);
            payload.put("textBody", textBody);
            payload.put("htmlBody", "");

            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(emailTopic, userId.toString(), json);
        } catch (Exception e) {
            log.warn("Failed to publish KYC email notification (best-effort). userId={}, status={}", userId, status, e);
        }
    }
}
