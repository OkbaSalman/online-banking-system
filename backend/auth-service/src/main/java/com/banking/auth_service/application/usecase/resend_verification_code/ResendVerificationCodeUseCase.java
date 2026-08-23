package com.banking.auth_service.application.usecase.resend_verification_code;

import com.banking.auth_service.application.usecase.resend_verification_code.dto.ResendVerificationCodeCommand;
import com.banking.auth_service.application.usecase.resend_verification_code.dto.ResendVerificationCodeResult;

public interface ResendVerificationCodeUseCase {
    ResendVerificationCodeResult resend(ResendVerificationCodeCommand command);
}
