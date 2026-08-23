package com.banking.cards_service.application.port;

import com.banking.transfers.v1.Transfer;

import java.util.UUID;

public interface TransfersClientPort {
    Transfer createTransfer(
            UUID fromAccountId,
            UUID toAccountId,
            long amountCents,
            String idempotencyKey,
            String description
    );
}
