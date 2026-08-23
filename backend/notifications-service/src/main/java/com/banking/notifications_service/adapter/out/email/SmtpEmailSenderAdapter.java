package com.banking.notifications_service.adapter.out.email;

import com.banking.notifications_service.application.port.EmailSenderPort;
import com.banking.notifications_service.domain.model.EmailMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Component
public class SmtpEmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSenderAdapter.class);

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSenderAdapter(JavaMailSender mailSender, @Value("${notifications.email.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(EmailMessage message) {
        try {
            String html = message.htmlBody();
            if (html != null && !html.isBlank()) {
                MimeMessage mime = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
                helper.setFrom(from);
                helper.setTo(message.to());
                helper.setSubject(message.subject());

                String text = message.textBody();
                helper.setText(text == null ? "" : text, html);

                mailSender.send(mime);
            } else {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setFrom(from);
                mail.setTo(message.to());
                mail.setSubject(message.subject());

                String body = message.textBody();
                mail.setText(body == null ? "" : body);

                mailSender.send(mail);
            }

            log.info("Email sent. messageId={}, to={}, subject={}", message.messageId(), message.to(), message.subject());
        } catch (Exception e) {
            log.warn("Failed to send email. messageId={}, to={}, subject={}", message.messageId(), message.to(), message.subject(), e);
            throw new RuntimeException(e);
        }
    }
}
