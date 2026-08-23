package com.banking.kyc_service.application.usecase.kyc.adminListPending;



import com.banking.kyc_service.domain.model.KycApplication;

import java.util.List;

public interface AdminListPendingUseCase {
    List<KycApplication> listPending(int limit, int offset);
}
