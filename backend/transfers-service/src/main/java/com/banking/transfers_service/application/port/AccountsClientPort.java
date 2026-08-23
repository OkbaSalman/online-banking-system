package com.banking.transfers_service.application.port;

import java.util.List;
import java.util.UUID;

public interface AccountsClientPort {

    CanDebitDecision canDebit(UUID accountId, UUID userId);

    List<AccountRef> listMyAccounts();

    List<AccountRef> listAccountsByType(AccountType type, int limit, int offset);
}
