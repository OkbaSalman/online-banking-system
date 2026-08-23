package com.banking.transfers_service.adapter.out.aml;

import com.banking.transfers_service.application.port.AmlDecision;
import com.banking.transfers_service.application.port.AmlPort;
import com.banking.transfers_service.application.port.TransferQueryPort;

import java.util.UUID;

public class RuleBasedAmlAdapter implements AmlPort {

    private final TransferQueryPort transfers;
    private final long maxSingleTransferCents;
    private final long velocityWindowMs;
    private final long velocityMaxCount;

    public RuleBasedAmlAdapter(
            TransferQueryPort transfers,
            long maxSingleTransferCents,
            long velocityWindowMs,
            long velocityMaxCount
    ) {
        this.transfers = transfers;
        this.maxSingleTransferCents = maxSingleTransferCents;
        this.velocityWindowMs = velocityWindowMs;
        this.velocityMaxCount = velocityMaxCount;
    }

    @Override
    public AmlDecision isTransferAllowed(UUID initiatorUserId, UUID fromAccountId, UUID toAccountId, long amountCents) {
        if (amountCents <= 0) {
            return AmlDecision.block("Invalid amount");
        }

        if (maxSingleTransferCents > 0 && amountCents > maxSingleTransferCents) {
            return AmlDecision.block("Amount exceeds AML single-transfer limit");
        }

        if (velocityMaxCount > 0 && velocityWindowMs > 0) {
            long since = System.currentTimeMillis() - velocityWindowMs;
            long cnt = transfers.countRecentTransfers(initiatorUserId, since);
            if (cnt >= velocityMaxCount) {
                return AmlDecision.block("Transfer velocity limit exceeded");
            }
        }

        return AmlDecision.allow();
    }
}
