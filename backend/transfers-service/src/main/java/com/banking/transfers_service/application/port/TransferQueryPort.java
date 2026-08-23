package com.banking.transfers_service.application.port;

import com.banking.transfers_service.domain.model.MonthlyRevenue;

import java.util.List;
import java.util.UUID;

public interface TransferQueryPort {
    long countRecentTransfers(UUID initiatorUserId, long sinceEpochMs);

    long countRecentCompletedDebits(UUID fromAccountId, long sinceEpochMs);

    List<MonthlyRevenue> aggregateCompletedRevenue(long fromEpochMs, long toEpochMsExclusive);
}
