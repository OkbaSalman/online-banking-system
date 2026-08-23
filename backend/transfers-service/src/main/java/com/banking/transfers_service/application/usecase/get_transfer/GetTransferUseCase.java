package com.banking.transfers_service.application.usecase.get_transfer;

import com.banking.transfers_service.application.usecase.get_transfer.dto.GetTransferQuery;
import com.banking.transfers_service.application.usecase.get_transfer.dto.GetTransferResult;

public interface GetTransferUseCase {
    GetTransferResult get(GetTransferQuery query);
}
