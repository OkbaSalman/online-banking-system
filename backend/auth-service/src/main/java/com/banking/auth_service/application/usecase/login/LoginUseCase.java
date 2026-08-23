package com.banking.auth_service.application.usecase.login;

import com.banking.auth_service.application.usecase.login.dto.LoginCommand;
import com.banking.auth_service.application.usecase.login.dto.LoginResult;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}