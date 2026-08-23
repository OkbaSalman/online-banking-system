package com.banking.auth_service.application.usecase.verify_email;
 
import com.banking.auth_service.application.usecase.verify_email.dto.VerifyEmailCommand;
import com.banking.auth_service.application.usecase.verify_email.dto.VerifyEmailResult;
 
public interface VerifyEmailUseCase {
    VerifyEmailResult verify(VerifyEmailCommand command);
}