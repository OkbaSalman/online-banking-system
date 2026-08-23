package com.banking.auth_service.application.usecase.request_password_reset;

import com.banking.auth_service.application.usecase.request_password_reset.dto.RequestPasswordResetCommand;
import com.banking.auth_service.application.usecase.request_password_reset.dto.RequestPasswordResetResult;

public interface RequestPasswordResetUseCase {
    RequestPasswordResetResult request(RequestPasswordResetCommand command);
}
