package com.banking.auth_service.application.usecase.logout;

import com.banking.auth_service.application.usecase.logout.dto.LogoutCommand;
import com.banking.auth_service.application.usecase.logout.dto.LogoutResult;

public interface LogoutUseCase {
    LogoutResult logout(LogoutCommand command);
}