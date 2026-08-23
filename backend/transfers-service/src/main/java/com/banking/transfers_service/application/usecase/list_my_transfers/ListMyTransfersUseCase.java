package com.banking.transfers_service.application.usecase.list_my_transfers;

import com.banking.transfers_service.application.usecase.list_my_transfers.dto.ListMyTransfersQuery;
import com.banking.transfers_service.application.usecase.list_my_transfers.dto.ListMyTransfersResult;

public interface ListMyTransfersUseCase {
    ListMyTransfersResult list(ListMyTransfersQuery query);
}
