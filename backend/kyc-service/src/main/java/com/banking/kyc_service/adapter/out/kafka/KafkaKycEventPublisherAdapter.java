package com.banking.kyc_service.adapter.out.kafka;

import com.banking.kyc_service.application.port.KycEventPublisherPort;
import com.banking.kyc_service.application.port.dto.KycStatusChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class KafkaKycEventPublisherAdapter implements KycEventPublisherPort {
    private static final Logger log = LoggerFactory.getLogger(KafkaKycEventPublisherAdapter.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaKycEventPublisherAdapter(KafkaTemplate<String, String> kafkaTemplate,
                                         ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(KycStatusChangedEvent event) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userId", event.userId().toString());
            payload.put("applicationId", event.applicationId().toString());
            payload.put("status", event.status());
            payload.put("reviewerUserId", event.reviewerUserId() == null ? "" : event.reviewerUserId());
            payload.put("rejectionReason", event.rejectionReason() == null ? "" : event.rejectionReason());
            payload.put("timestampEpochMs", event.timestampEpochMs());

            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send("kyc-events", event.userId().toString(), json);
        } catch (Exception e) {
            log.warn("Failed to publish KYC event (best-effort). userId={}, applicationId={}, status={}",
                    event.userId(), event.applicationId(), event.status(), e);
        }
    }
}
