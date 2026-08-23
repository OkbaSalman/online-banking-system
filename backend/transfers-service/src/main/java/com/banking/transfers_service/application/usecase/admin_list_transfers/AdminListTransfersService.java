package com.banking.transfers_service.application.usecase.admin_list_transfers;

import com.banking.transfers_service.application.port.TransferRepositoryPort;
import com.banking.transfers_service.application.usecase.admin_list_transfers.dto.AdminListTransfersQuery;
import com.banking.transfers_service.application.usecase.admin_list_transfers.dto.AdminListTransfersResult;

public class AdminListTransfersService implements AdminListTransfersUseCase {

    private final TransferRepositoryPort transfers;

    public AdminListTransfersService(TransferRepositoryPort transfers) {
        this.transfers = transfers;
    }

    @Override
    public AdminListTransfersResult list(AdminListTransfersQuery query) {
        validate(query);

        var res = transfers.adminList(
                query.status(),
                query.initiatorUserId(),
                query.fromAccountId(),
                query.toAccountId(),
                query.limit(),
                query.offset()
        );

        return new AdminListTransfersResult(res);
    }

    private static void validate(AdminListTransfersQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query is required");
        }
    }
}
