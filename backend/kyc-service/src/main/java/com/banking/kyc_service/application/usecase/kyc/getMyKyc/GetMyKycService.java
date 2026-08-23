package com.banking.kyc_service.application.usecase.kyc.getMyKyc;


import com.banking.kyc_service.application.port.KycApplicationRepositoryPort;
import com.banking.kyc_service.application.usecase.kyc.getOrCreateDraft.GetOrCreateDraftUseCase;
import com.banking.kyc_service.domain.model.KycApplication;

import java.util.UUID;

public class GetMyKycService implements GetMyKycUseCase {
    private final KycApplicationRepositoryPort apps;
    private final GetOrCreateDraftUseCase getOrCreateDraft;

    public GetMyKycService(KycApplicationRepositoryPort apps, GetOrCreateDraftUseCase getOrCreateDraft) {
        this.apps = apps;
        this.getOrCreateDraft = getOrCreateDraft;
    }

    @Override
    public KycApplication getMyKyc(UUID userId) {
        return apps.findByUserId(userId).orElseGet(() -> getOrCreateDraft.getOrCreateDraft(userId));
    }
}