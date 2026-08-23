package com.banking.kyc_service.application.usecase.kyc.adminReview;

import com.banking.kyc_service.application.usecase.kyc.adminReview.dto.AdminReviewCommand;
import com.banking.kyc_service.domain.model.KycApplication;

public interface AdminReviewUseCase {
    KycApplication review(AdminReviewCommand command);
}
