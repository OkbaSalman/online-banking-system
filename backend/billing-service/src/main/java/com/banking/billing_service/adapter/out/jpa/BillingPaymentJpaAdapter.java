package com.banking.billing_service.adapter.out.jpa;

import com.banking.billing_service.adapter.out.jpa.entity.BillingPaymentEntity;
import com.banking.billing_service.adapter.out.jpa.repository.BillingPaymentJpaRepository;
import com.banking.billing_service.application.port.BillingPaymentQueryPort;
import com.banking.billing_service.application.port.BillingPaymentRepositoryPort;
import com.banking.billing_service.domain.model.BillingPayment;
import com.banking.billing_service.domain.model.BillingPaymentStatus;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BillingPaymentJpaAdapter implements BillingPaymentRepositoryPort, BillingPaymentQueryPort {

    private final BillingPaymentJpaRepository repo;

    public BillingPaymentJpaAdapter(BillingPaymentJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<BillingPayment> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey) {
        return repo.findByUserIdAndIdempotencyKey(userId, idempotencyKey).map(this::toDomain);
    }

    @Override
    public BillingPayment save(BillingPayment payment) {
        BillingPaymentEntity saved = repo.save(toEntity(payment));
        return toDomain(saved);
    }

    @Override
    public List<BillingPayment> listByUserId(UUID userId, int limit, int offset) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<BillingPaymentEntity> pageItems = repo.listByUserId(userId, PageRequest.of(page, safeLimit + offsetInPage));
        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<BillingPayment> listBySubscriptionId(UUID subscriptionId, int limit, int offset) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<BillingPaymentEntity> pageItems = repo.listBySubscriptionId(subscriptionId, PageRequest.of(page, safeLimit + offsetInPage));
        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    private BillingPayment toDomain(BillingPaymentEntity e) {
        return new BillingPayment(
                e.getId(),
                e.getUserId(),
                e.getFromAccountId(),
                e.getMerchantAccountId(),
                e.getAmountCents(),
                e.getCreatedAtEpochMs(),
                BillingPaymentStatus.valueOf(e.getStatus()),
                e.getIdempotencyKey(),
                e.getDescription(),
                e.getTransferId(),
                e.getFailureMessage(),
                e.getSubscriptionId()
        );
    }

    private BillingPaymentEntity toEntity(BillingPayment p) {
        return new BillingPaymentEntity(
                p.id(),
                p.userId(),
                p.fromAccountId(),
                p.merchantAccountId(),
                p.amountCents(),
                p.createdAtEpochMs(),
                p.status().name(),
                p.idempotencyKey(),
                p.description(),
                p.transferId(),
                p.failureMessage(),
                p.subscriptionId()
        );
    }
}
