package com.banking.transfers_service.application.port;

import com.banking.transfers_service.domain.model.Transfer;
import com.banking.transfers_service.domain.model.TransferStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepositoryPort {

    Optional<Transfer> findByInitiatorUserIdAndIdempotencyKey(UUID initiatorUserId, String idempotencyKey);

    Optional<Transfer> findById(UUID transferId);

    List<Transfer> listByInitiatorUserId(
            UUID initiatorUserId,
            TransferStatus status,
            UUID fromAccountId,
            UUID toAccountId,
            int limit,
            int offset
    );

    List<Transfer> listVisibleToUser(
            UUID userId,
            List<UUID> accountIds,
            TransferStatus status,
            UUID fromAccountId,
            UUID toAccountId,
            int limit,
            int offset
    );

    List<Transfer> adminList(
            TransferStatus status,
            UUID initiatorUserId,
            UUID fromAccountId,
            UUID toAccountId,
            int limit,
            int offset
    );

    Transfer save(Transfer transfer);
}
