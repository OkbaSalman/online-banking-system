package com.banking.kyc_service.application.usecase.kyc.adminListPending;


import com.banking.kyc_service.application.port.KycApplicationRepositoryPort;
import com.banking.kyc_service.domain.model.KycApplication;

import java.util.List;

public class AdminListPendingService implements AdminListPendingUseCase {
    private final KycApplicationRepositoryPort apps;

    public AdminListPendingService(KycApplicationRepositoryPort apps) {
        this.apps = apps;
    }

    @Override
    public List<KycApplication> listPending(int limit, int offset) {
        return apps.findPending(limit, offset);
    }
}
