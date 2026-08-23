package com.banking.kyc_service.application.usecase.kyc.submitKyc;



import com.banking.kyc_service.application.usecase.kyc.submitKyc.dto.SubmitKycCommand;
import com.banking.kyc_service.domain.model.KycApplication;

public interface SubmitKycUseCase {
    KycApplication submit(SubmitKycCommand command);
}