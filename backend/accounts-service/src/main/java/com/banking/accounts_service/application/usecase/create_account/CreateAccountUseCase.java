package com.banking.accounts_service.application.usecase.create_account;

import com.banking.accounts_service.application.usecase.create_account.dto.CreateAccountCommand;
import com.banking.accounts_service.application.usecase.create_account.dto.CreateAccountResult;

public interface CreateAccountUseCase {
    CreateAccountResult create(CreateAccountCommand command);
}
