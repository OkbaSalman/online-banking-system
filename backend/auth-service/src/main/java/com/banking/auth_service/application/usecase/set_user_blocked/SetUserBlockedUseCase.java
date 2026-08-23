package com.banking.auth_service.application.usecase.set_user_blocked;

import com.banking.auth_service.application.usecase.set_user_blocked.dto.SetUserBlockedCommand;
import com.banking.auth_service.application.usecase.set_user_blocked.dto.SetUserBlockedResult;

public interface SetUserBlockedUseCase {
    SetUserBlockedResult setBlocked(SetUserBlockedCommand command);
}
