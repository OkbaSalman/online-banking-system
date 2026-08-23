package com.banking.kyc_service.application.port;

import com.banking.kyc_service.domain.model.KycApplication;
import com.banking.kyc_service.domain.model.KycStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KycApplicationRepositoryPort {
    Optional<KycApplication> findByUserId(UUID userId);
    Optional<KycApplication> findById(UUID id);
    KycApplication save(KycApplication app);
    List<KycApplication> findPending(int limit, int offset);

    List<KycApplication> findByStatuses(List<KycStatus> statuses, int limit, int offset);
}