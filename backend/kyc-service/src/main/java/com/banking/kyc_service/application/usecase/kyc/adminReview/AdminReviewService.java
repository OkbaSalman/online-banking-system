package com.banking.kyc_service.application.usecase.kyc.adminReview;

import com.banking.kyc_service.application.port.KycApplicationRepositoryPort;
import com.banking.kyc_service.application.port.EmailNotificationPort;
import com.banking.kyc_service.application.port.KycEventPublisherPort;
import com.banking.kyc_service.application.port.dto.KycStatusChangedEvent;
import com.banking.kyc_service.application.usecase.kyc.adminReview.dto.AdminReviewCommand;
import com.banking.kyc_service.domain.model.KycApplication;
import com.banking.kyc_service.domain.model.KycStatus;

import java.time.Instant;

public class AdminReviewService implements AdminReviewUseCase {
    private final KycApplicationRepositoryPort apps;
    private final KycEventPublisherPort events;
    private final EmailNotificationPort emailNotifications;

    public AdminReviewService(
            KycApplicationRepositoryPort apps,
            KycEventPublisherPort events,
            EmailNotificationPort emailNotifications
    ) {
        this.apps = apps;
        this.events = events;
        this.emailNotifications = emailNotifications;
    }

    @Override
    public KycApplication review(AdminReviewCommand command) {
        var app = apps.findById(command.applicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        if (app.status() != KycStatus.PENDING) {
            throw new IllegalStateException("Application not in PENDING state");
        }

        Instant now = Instant.now();
        KycApplication updated = new KycApplication(
                app.id(),
                app.userId(),
                command.approve() ? KycStatus.APPROVED : KycStatus.REJECTED,
                app.fullName(),
                app.nationalId(),
                app.address(),
                command.reviewerUserId(),
                command.rejectionReason(),
                app.createdAt(),
                now
        );

        KycApplication saved = apps.save(updated);

        events.publish(new KycStatusChangedEvent(
                saved.userId(),
                saved.id(),
                command.approve() ? "APPROVED" : "REJECTED",
                command.reviewerUserId().toString(),
                command.rejectionReason() == null ? "" : command.rejectionReason(),
                System.currentTimeMillis()
        ));

        emailNotifications.sendKycStatusChanged(
                saved.userId(),
                command.approve() ? "APPROVED" : "REJECTED",
                command.rejectionReason()
        );

        return saved;
    }
}