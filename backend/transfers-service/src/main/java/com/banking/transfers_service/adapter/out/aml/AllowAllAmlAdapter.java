package com.banking.transfers_service.adapter.out.aml;

import com.banking.transfers_service.application.port.AmlDecision;
import com.banking.transfers_service.application.port.AmlPort;

import java.util.UUID;

public class AllowAllAmlAdapter implements AmlPort {
    @Override
    public AmlDecision isTransferAllowed(UUID initiatorUserId, UUID fromAccountId, UUID toAccountId, long amountCents) {
        return AmlDecision.allow();
    }
}
