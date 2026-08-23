package com.banking.kyc_service.application.usecase.kyc.getOrCreateDraft;

import com.banking.kyc_service.application.port.KycApplicationRepositoryPort;
import com.banking.kyc_service.domain.model.KycApplication;
import com.banking.kyc_service.domain.model.KycStatus;

import java.time.Instant;
import java.util.UUID;

public class GetOrCreateDraftService implements GetOrCreateDraftUseCase {
    private final KycApplicationRepositoryPort apps;

    public GetOrCreateDraftService(KycApplicationRepositoryPort apps) {
        this.apps = apps;
    }

    @Override
    public KycApplication getOrCreateDraft(UUID userId) {
        return apps.findByUserId(userId).orElseGet(() -> {
            Instant now = Instant.now();
            KycApplication draft = new KycApplication(
                    UUID.randomUUID(),
                    userId,
                    KycStatus.NOT_SUBMITTED,
                    "",
                    "",
                    "",
                    null,
                    null,
                    now,
                    now
            );
            return apps.save(draft);
        });
    }
}