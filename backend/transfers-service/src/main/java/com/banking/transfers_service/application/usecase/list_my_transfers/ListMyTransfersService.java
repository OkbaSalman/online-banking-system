package com.banking.transfers_service.application.usecase.list_my_transfers;

import com.banking.transfers_service.application.port.AccountRef;
import com.banking.transfers_service.application.port.AccountsClientPort;
import com.banking.transfers_service.application.port.TransferRepositoryPort;
import com.banking.transfers_service.application.usecase.list_my_transfers.dto.ListMyTransfersQuery;
import com.banking.transfers_service.application.usecase.list_my_transfers.dto.ListMyTransfersResult;

import java.util.List;
import java.util.UUID;

public class ListMyTransfersService implements ListMyTransfersUseCase {

    private final TransferRepositoryPort transfers;
    private final AccountsClientPort accounts;

    public ListMyTransfersService(TransferRepositoryPort transfers, AccountsClientPort accounts) {
        this.transfers = transfers;
        this.accounts = accounts;
    }

    @Override
    public ListMyTransfersResult list(ListMyTransfersQuery query) {
        validate(query);

        List<AccountRef> myAccounts = accounts.listMyAccounts();
        List<UUID> accountIds = myAccounts.stream().map(AccountRef::accountId).toList();

        var res = transfers.listVisibleToUser(
                query.requesterUserId(),
                accountIds,
                query.status(),
                query.fromAccountId(),
                query.toAccountId(),
                query.limit(),
                query.offset()
        );
        return new ListMyTransfersResult(res);
    }

    private static void validate(ListMyTransfersQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query is required");
        }
        if (query.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
    }
}
