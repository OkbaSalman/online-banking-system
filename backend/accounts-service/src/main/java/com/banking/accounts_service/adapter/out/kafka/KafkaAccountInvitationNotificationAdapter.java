package com.banking.accounts_service.adapter.out.kafka;

import com.banking.accounts_service.application.port.AccountInvitationNotificationPort;
import com.banking.accounts_service.domain.model.Account;
import com.banking.accounts_service.domain.model.AccountInvitation;
import com.banking.auth.v1.AuthServiceGrpc;
import com.banking.auth.v1.GetUserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class KafkaAccountInvitationNotificationAdapter implements AccountInvitationNotificationPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaAccountInvitationNotificationAdapter.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AuthServiceGrpc.AuthServiceBlockingStub auth;
    private final String emailTopic;

    public KafkaAccountInvitationNotificationAdapter(
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
    public void sendInvitationRequested(AccountInvitation invitation, Account account) {
        try {
            var user = auth.getUser(GetUserRequest.newBuilder().setUserId(invitation.invitedUserId().toString()).build());
            String email = user.getEmail();
            if (email == null || email.isBlank()) {
                log.warn("Cannot send invitation email: empty email. invitedUserId={}, invitationId={}", invitation.invitedUserId(), invitation.id());
                return;
            }

            String subject = "Joint account invitation";
            String textBody = "You have been invited to join a joint account.\n" +
                    "IBAN: " + account.iban() + "\n" +
                    "Role: " + invitation.role().name() + "\n" +
                    "Invitation ID: " + invitation.id();

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("messageId", "invite:" + invitation.id() + ":" + System.currentTimeMillis());
            payload.put("to", email);
            payload.put("subject", subject);
            payload.put("textBody", textBody);
            payload.put("htmlBody", "");

            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(emailTopic, invitation.invitedUserId().toString(), json);
        } catch (Exception e) {
            log.warn("Failed to publish invitation email notification (best-effort). invitationId={}, accountId={}", invitation.id(), invitation.accountId(), e);
        }
    }
}
