package com.banking.kyc_service.application.usecase.kyc.adminListApplications;

import com.banking.kyc_service.application.port.KycApplicationRepositoryPort;
import com.banking.kyc_service.domain.model.KycApplication;
import com.banking.kyc_service.domain.model.KycStatus;

import java.util.List;

public class AdminListApplicationsService implements AdminListApplicationsUseCase {

    private static final List<KycStatus> REVIEWED = List.of(KycStatus.APPROVED, KycStatus.REJECTED);

    private final KycApplicationRepositoryPort apps;

    public AdminListApplicationsService(KycApplicationRepositoryPort apps) {
        this.apps = apps;
    }

    @Override
    public List<KycApplication> list(KycStatus status, int limit, int offset) {
        if (status == KycStatus.PENDING) {
            return apps.findPending(limit, offset);
        }
        if (status == KycStatus.APPROVED || status == KycStatus.REJECTED) {
            return apps.findByStatuses(List.of(status), limit, offset);
        }
        return apps.findByStatuses(REVIEWED, limit, offset);
    }
}
