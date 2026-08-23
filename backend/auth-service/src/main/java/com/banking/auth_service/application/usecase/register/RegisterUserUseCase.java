package com.banking.auth_service.application.usecase.register;

import com.banking.auth_service.application.usecase.register.dto.RegisterUserCommand;
import com.banking.auth_service.application.usecase.register.dto.RegisterUserResult;

public interface RegisterUserUseCase {
    RegisterUserResult register(RegisterUserCommand command);
}
