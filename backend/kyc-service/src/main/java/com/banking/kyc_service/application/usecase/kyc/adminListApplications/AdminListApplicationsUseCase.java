package com.banking.kyc_service.application.usecase.kyc.adminListApplications;

import com.banking.kyc_service.domain.model.KycApplication;
import com.banking.kyc_service.domain.model.KycStatus;

import java.util.List;

public interface AdminListApplicationsUseCase {
    List<KycApplication> list(KycStatus status, int limit, int offset);
}
