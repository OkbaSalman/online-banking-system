package com.banking.kyc_service.application.usecase.kyc.getMyKyc;

import com.banking.kyc_service.domain.model.KycApplication;

import java.util.UUID;

public interface GetMyKycUseCase {
    KycApplication getMyKyc(UUID userId);
}