package com.banking.accounts_service.application.usecase.set_account_frozen;

import com.banking.accounts_service.application.usecase.set_account_frozen.dto.SetAccountFrozenCommand;
import com.banking.accounts_service.application.usecase.set_account_frozen.dto.SetAccountFrozenResult;

public interface SetAccountFrozenUseCase {
    SetAccountFrozenResult setFrozen(SetAccountFrozenCommand command);
}
