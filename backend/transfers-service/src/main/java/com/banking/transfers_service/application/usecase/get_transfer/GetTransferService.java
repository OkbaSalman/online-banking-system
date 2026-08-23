package com.banking.transfers_service.application.usecase.get_transfer;

import com.banking.transfers_service.application.port.TransferRepositoryPort;
import com.banking.transfers_service.application.usecase.common.exception.NotFoundException;
import com.banking.transfers_service.application.usecase.get_transfer.dto.GetTransferQuery;
import com.banking.transfers_service.application.usecase.get_transfer.dto.GetTransferResult;

public class GetTransferService implements GetTransferUseCase {

    private final TransferRepositoryPort transfers;

    public GetTransferService(TransferRepositoryPort transfers) {
        this.transfers = transfers;
    }

    @Override
    public GetTransferResult get(GetTransferQuery query) {
        var t = transfers.findById(query.transferId())
                .orElseThrow(() -> new NotFoundException("Transfer not found"));
        return new GetTransferResult(t);
    }
}
