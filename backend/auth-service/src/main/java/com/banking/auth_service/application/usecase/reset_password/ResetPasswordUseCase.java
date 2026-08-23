package com.banking.auth_service.application.usecase.reset_password;

import com.banking.auth_service.application.usecase.reset_password.dto.ResetPasswordCommand;
import com.banking.auth_service.application.usecase.reset_password.dto.ResetPasswordResult;

public interface ResetPasswordUseCase {
    ResetPasswordResult reset(ResetPasswordCommand command);
}
