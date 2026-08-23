package com.banking.transfers_service.application.port;

import java.util.UUID;

public interface AmlPort {
    AmlDecision isTransferAllowed(UUID initiatorUserId, UUID fromAccountId, UUID toAccountId, long amountCents);
}
