package com.banking.auth_service.application.usecase.refresh;

import com.banking.auth_service.application.usecase.refresh.dto.RefreshCommand;
import com.banking.auth_service.application.usecase.refresh.dto.RefreshResult;

public interface RefreshUseCase {
    RefreshResult refresh(RefreshCommand command);
}