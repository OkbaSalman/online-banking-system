package com.banking.kyc_service.application.usecase.kyc.submitKyc;

import com.banking.kyc_service.application.port.KycApplicationRepositoryPort;
import com.banking.kyc_service.application.port.KycDocumentRepositoryPort;
import com.banking.kyc_service.application.port.KycEventPublisherPort;
import com.banking.kyc_service.application.port.dto.KycStatusChangedEvent;
import com.banking.kyc_service.application.usecase.kyc.submitKyc.dto.SubmitKycCommand;
import com.banking.kyc_service.domain.model.KycApplication;
import com.banking.kyc_service.domain.model.KycStatus;

import java.time.Instant;

public class SubmitKycService implements SubmitKycUseCase {
    private final KycApplicationRepositoryPort apps;
    private final KycDocumentRepositoryPort docs;
    private final KycEventPublisherPort events;

    public SubmitKycService(
            KycApplicationRepositoryPort apps,
            KycDocumentRepositoryPort docs,
            KycEventPublisherPort events
    ) {
        this.apps = apps;
        this.docs = docs;
        this.events = events;
    }

    @Override
    public KycApplication submit(SubmitKycCommand command) {
        var existing = apps.findByUserId(command.userId())
                .orElseThrow(() -> new IllegalArgumentException("No draft KYC found"));

        if (existing.status() == KycStatus.PENDING) {
            throw new IllegalStateException("KYC already submitted");
        }

        var foundDocs = docs.findAllById(command.documentIds());
        if (foundDocs.size() != command.documentIds().size()) {
            throw new IllegalArgumentException("Some documents not found");
        }

        Instant now = Instant.now();
        KycApplication updated = new KycApplication(
                existing.id(),
                existing.userId(),
                KycStatus.PENDING,
                command.fullName(),
                command.nationalId(),
                command.address(),
                existing.reviewerUserId(),
                existing.rejectionReason(),
                existing.createdAt(),
                now
        );

        KycApplication saved = apps.save(updated);

        events.publish(new KycStatusChangedEvent(
                saved.userId(),
                saved.id(),
                "SUBMITTED",
                "",
                "",
                System.currentTimeMillis()
        ));

        return saved;
    }
}
