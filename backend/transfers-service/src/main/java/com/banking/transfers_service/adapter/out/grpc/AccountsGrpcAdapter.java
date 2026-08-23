package com.banking.transfers_service.adapter.out.grpc;

import com.banking.accounts.v1.AccountType;
import com.banking.accounts.v1.AccountsServiceGrpc;
import com.banking.accounts.v1.CanDebitRequest;
import com.banking.accounts.v1.ListMyAccountsRequest;
import com.banking.accounts.v1.ListAccountsByTypeRequest;
import com.banking.transfers_service.application.port.AccountsClientPort;
import com.banking.transfers_service.application.port.AccountRef;
import com.banking.transfers_service.application.port.CanDebitDecision;

import java.util.List;
import java.util.UUID;

public class AccountsGrpcAdapter implements AccountsClientPort {

    private final AccountsServiceGrpc.AccountsServiceBlockingStub accounts;

    public AccountsGrpcAdapter(AccountsServiceGrpc.AccountsServiceBlockingStub accounts) {
        this.accounts = accounts;
    }

    @Override
    public CanDebitDecision canDebit(UUID accountId, UUID userId) {
        var res = accounts.canDebit(CanDebitRequest.newBuilder()
                .setAccountId(accountId.toString())
                .setUserId(userId.toString())
                .build());

        return new CanDebitDecision(
                res.getAllowed(),
                res.getReason(),
                toDomainType(res.getAccountType())
        );
    }

    @Override
    public List<AccountRef> listMyAccounts() {
        var res = accounts.listMyAccounts(ListMyAccountsRequest.newBuilder().build());
        return res.getAccountsList().stream()
                .map(a -> new AccountRef(UUID.fromString(a.getId())))
                .toList();
    }

    @Override
    public List<AccountRef> listAccountsByType(
            com.banking.transfers_service.application.port.AccountType type,
            int limit,
            int offset
    ) {
        var res = accounts.listAccountsByType(ListAccountsByTypeRequest.newBuilder()
                .setAccountType(toProtoType(type))
                .setLimit(limit)
                .setOffset(offset)
                .build());

        return res.getAccountsList().stream()
                .map(a -> new AccountRef(UUID.fromString(a.getId())))
                .toList();
    }

    private static com.banking.transfers_service.application.port.AccountType toDomainType(AccountType type) {
        if (type == null) {
            return com.banking.transfers_service.application.port.AccountType.CHECKING;
        }
        return switch (type) {
            case ACCOUNT_TYPE_SAVINGS -> com.banking.transfers_service.application.port.AccountType.SAVINGS;
            case ACCOUNT_TYPE_CHECKING, ACCOUNT_TYPE_UNSPECIFIED, UNRECOGNIZED -> com.banking.transfers_service.application.port.AccountType.CHECKING;
        };
    }

    private static AccountType toProtoType(com.banking.transfers_service.application.port.AccountType type) {
        if (type == null) {
            return AccountType.ACCOUNT_TYPE_UNSPECIFIED;
        }
        return switch (type) {
            case SAVINGS -> AccountType.ACCOUNT_TYPE_SAVINGS;
            case CHECKING -> AccountType.ACCOUNT_TYPE_CHECKING;
        };
    }
}
