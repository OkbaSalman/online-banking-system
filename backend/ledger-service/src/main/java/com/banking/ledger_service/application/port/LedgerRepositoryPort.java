package com.banking.ledger_service.application.port;

import com.banking.ledger_service.domain.model.AccountLedgerItem;
import com.banking.ledger_service.domain.model.Balance;
import com.banking.ledger_service.domain.model.ChainHead;
import com.banking.ledger_service.domain.model.LedgerEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerRepositoryPort {

    Optional<LedgerEntry> findByIdempotencyKey(UUID initiatorUserId, String idempotencyKey);

    LedgerWriteResult createTransfer(
            UUID initiatorUserId,
            UUID fromAccountId,
            UUID toAccountId,
            long amountCents,
            String idempotencyKey,
            String description
    );

    Balance getBalance(UUID accountId);

    LedgerEntry getEntry(UUID entryId);

    List<AccountLedgerItem> listAccountEntries(UUID accountId, int limit, int offset);

    ChainHead getChainHead(UUID accountId);

    VerifyChainScanResult verifyChain(UUID accountId);
}