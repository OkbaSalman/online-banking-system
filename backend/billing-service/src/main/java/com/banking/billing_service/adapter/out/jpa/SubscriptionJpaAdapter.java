package com.banking.billing_service.adapter.out.jpa;

import com.banking.billing_service.adapter.out.jpa.entity.SubscriptionEntity;
import com.banking.billing_service.adapter.out.jpa.repository.SubscriptionJpaRepository;
import com.banking.billing_service.application.port.SubscriptionQueryPort;
import com.banking.billing_service.application.port.SubscriptionRepositoryPort;
import com.banking.billing_service.domain.model.IntervalUnit;
import com.banking.billing_service.domain.model.Subscription;
import com.banking.billing_service.domain.model.SubscriptionStatus;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SubscriptionJpaAdapter implements SubscriptionRepositoryPort, SubscriptionQueryPort {

    private final SubscriptionJpaRepository repo;

    public SubscriptionJpaAdapter(SubscriptionJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Subscription> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey) {
        return repo.findByUserIdAndIdempotencyKey(userId, idempotencyKey).map(this::toDomain);
    }

    @Override
    public Optional<Subscription> findById(UUID subscriptionId) {
        return repo.findById(subscriptionId).map(this::toDomain);
    }

    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionEntity saved = repo.save(toEntity(subscription));
        return toDomain(saved);
    }

    @Override
    public List<Subscription> listByUserId(UUID userId, int limit, int offset) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<SubscriptionEntity> pageItems = repo.listByUserId(userId, PageRequest.of(page, safeLimit + offsetInPage));
        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Subscription> listDueActive(long nowEpochMs, int limit) {
        int safeLimit = limit <= 0 ? 25 : Math.min(limit, 200);
        List<SubscriptionEntity> items = repo.listDueActive(nowEpochMs, PageRequest.of(0, safeLimit));
        return items.stream().map(this::toDomain).toList();
    }

    private Subscription toDomain(SubscriptionEntity e) {
        long dueAnchor = e.getDueAnchorEpochMs() > 0 ? e.getDueAnchorEpochMs() : e.getNextChargeAtEpochMs();
        return new Subscription(
                e.getId(),
                e.getUserId(),
                e.getFromAccountId(),
                e.getMerchantAccountId(),
                e.getAmountCents(),
                IntervalUnit.valueOf(e.getIntervalUnit()),
                e.getIntervalCount(),
                e.getNextChargeAtEpochMs(),
                SubscriptionStatus.valueOf(e.getStatus()),
                e.getCreatedAtEpochMs(),
                e.getIdempotencyKey(),
                e.getDescription(),
                e.getLastAttemptAtEpochMs(),
                e.getConsecutiveFailures(),
                dueAnchor
        );
    }

    private SubscriptionEntity toEntity(Subscription s) {
        return new SubscriptionEntity(
                s.id(),
                s.userId(),
                s.fromAccountId(),
                s.merchantAccountId(),
                s.amountCents(),
                s.intervalUnit().name(),
                s.intervalCount(),
                s.nextChargeAtEpochMs(),
                s.status().name(),
                s.createdAtEpochMs(),
                s.idempotencyKey(),
                s.description(),
                s.lastAttemptAtEpochMs(),
                s.consecutiveFailures(),
                s.dueAnchorEpochMs()
        );
    }
}
