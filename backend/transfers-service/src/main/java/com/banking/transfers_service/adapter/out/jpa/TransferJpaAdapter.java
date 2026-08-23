package com.banking.transfers_service.adapter.out.jpa;

import com.banking.transfers_service.adapter.out.jpa.entity.TransferEntity;
import com.banking.transfers_service.adapter.out.jpa.repository.TransferJpaRepository;
import com.banking.transfers_service.application.port.TransferRepositoryPort;
import com.banking.transfers_service.application.port.TransferQueryPort;
import com.banking.transfers_service.domain.model.MonthlyRevenue;
import com.banking.transfers_service.domain.model.Transfer;
import com.banking.transfers_service.domain.model.TransferStatus;

import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TransferJpaAdapter implements TransferRepositoryPort, TransferQueryPort {

    private final TransferJpaRepository repo;

    public TransferJpaAdapter(TransferJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Transfer> findByInitiatorUserIdAndIdempotencyKey(UUID initiatorUserId, String idempotencyKey) {
        return repo.findByInitiatorUserIdAndIdempotencyKey(initiatorUserId, idempotencyKey).map(this::toDomain);
    }

    @Override
    public Optional<Transfer> findById(UUID transferId) {
        return repo.findById(transferId).map(this::toDomain);
    }

    @Override
    public List<Transfer> listByInitiatorUserId(
            UUID initiatorUserId,
            TransferStatus status,
            UUID fromAccountId,
            UUID toAccountId,
            int limit,
            int offset
    ) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<TransferEntity> pageItems = repo.listMyTransfers(
                initiatorUserId,
                status == null ? null : status.name(),
                fromAccountId,
                toAccountId,
                PageRequest.of(page, safeLimit + offsetInPage)
        );

        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Transfer> listVisibleToUser(
            UUID userId,
            List<UUID> accountIds,
            TransferStatus status,
            UUID fromAccountId,
            UUID toAccountId,
            int limit,
            int offset
    ) {
        if (accountIds == null || accountIds.isEmpty()) {
            return listByInitiatorUserId(userId, status, fromAccountId, toAccountId, limit, offset);
        }

        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<TransferEntity> pageItems = repo.listVisibleToUser(
                userId,
                accountIds,
                status == null ? null : status.name(),
                fromAccountId,
                toAccountId,
                PageRequest.of(page, safeLimit + offsetInPage)
        );

        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Transfer> adminList(
            TransferStatus status,
            UUID initiatorUserId,
            UUID fromAccountId,
            UUID toAccountId,
            int limit,
            int offset
    ) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<TransferEntity> pageItems = repo.adminListTransfers(
                initiatorUserId,
                status == null ? null : status.name(),
                fromAccountId,
                toAccountId,
                PageRequest.of(page, safeLimit + offsetInPage)
        );

        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Transfer save(Transfer transfer) {
        TransferEntity saved = repo.save(toEntity(transfer));
        return toDomain(saved);
    }

    @Override
    public long countRecentTransfers(UUID initiatorUserId, long sinceEpochMs) {
        return repo.countByInitiatorUserIdAndCreatedAtEpochMsGreaterThan(initiatorUserId, sinceEpochMs);
    }

    @Override
    public long countRecentCompletedDebits(UUID fromAccountId, long sinceEpochMs) {
        return repo.countByFromAccountIdAndStatusAndCreatedAtEpochMsGreaterThan(fromAccountId, TransferStatus.COMPLETED.name(), sinceEpochMs);
    }

    @Override
    public List<MonthlyRevenue> aggregateCompletedRevenue(long fromEpochMs, long toEpochMsExclusive) {
        return repo.aggregateCompletedRevenue(fromEpochMs, toEpochMsExclusive).stream()
                .map(row -> new MonthlyRevenue(
                        toInt(row[0]),
                        toInt(row[1]),
                        toLong(row[2]),
                        toLong(row[3]),
                        toInt(row[4])
                ))
                .toList();
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalStateException("Expected numeric year/month/count, got " + value);
    }

    private static long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalStateException("Expected numeric amount, got " + value);
    }

    private Transfer toDomain(TransferEntity e) {
        return new Transfer(
                e.getId(),
                e.getInitiatorUserId(),
                e.getFromAccountId(),
                e.getToAccountId(),
                e.getAmountCents(),
                e.getFeeCents(),
                e.getIdempotencyKey(),
                e.getDescription(),
                e.getCreatedAtEpochMs(),
                TransferStatus.valueOf(e.getStatus()),
                e.getLedgerEntryId(),
                e.getFeeLedgerEntryId(),
                e.getFailureMessage()
        );
    }

    private TransferEntity toEntity(Transfer t) {
        return new TransferEntity(
                t.id(),
                t.initiatorUserId(),
                t.fromAccountId(),
                t.toAccountId(),
                t.amountCents(),
                t.feeCents(),
                t.idempotencyKey(),
                t.description(),
                t.createdAtEpochMs(),
                t.status().name(),
                t.ledgerEntryId(),
                t.feeLedgerEntryId(),
                t.failureMessage()
        );
    }
}
