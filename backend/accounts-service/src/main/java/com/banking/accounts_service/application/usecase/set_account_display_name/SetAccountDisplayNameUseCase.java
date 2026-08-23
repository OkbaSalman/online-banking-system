package com.banking.accounts_service.application.usecase.set_account_display_name;

import com.banking.accounts_service.application.usecase.set_account_display_name.dto.SetAccountDisplayNameCommand;
import com.banking.accounts_service.application.usecase.set_account_display_name.dto.SetAccountDisplayNameResult;

public interface SetAccountDisplayNameUseCase {
    SetAccountDisplayNameResult setDisplayName(SetAccountDisplayNameCommand command);
}
