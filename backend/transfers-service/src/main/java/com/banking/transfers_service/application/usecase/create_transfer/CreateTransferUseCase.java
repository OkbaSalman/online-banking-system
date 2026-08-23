package com.banking.transfers_service.application.usecase.create_transfer;

import com.banking.transfers_service.application.usecase.create_transfer.dto.CreateTransferCommand;
import com.banking.transfers_service.application.usecase.create_transfer.dto.CreateTransferResult;

public interface CreateTransferUseCase {
    CreateTransferResult create(CreateTransferCommand command);
}
