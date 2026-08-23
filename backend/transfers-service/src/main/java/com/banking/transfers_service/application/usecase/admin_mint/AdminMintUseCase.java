package com.banking.transfers_service.application.usecase.admin_mint;

import com.banking.transfers_service.application.usecase.admin_mint.dto.AdminMintCommand;
import com.banking.transfers_service.application.usecase.admin_mint.dto.AdminMintResult;

public interface AdminMintUseCase {
    AdminMintResult mint(AdminMintCommand command);
}
