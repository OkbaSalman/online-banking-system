package com.banking.cards_service.adapter.out.jpa;

import com.banking.cards_service.adapter.out.jpa.entity.CardChargeEntity;
import com.banking.cards_service.adapter.out.jpa.repository.CardChargeJpaRepository;
import com.banking.cards_service.application.port.CardChargeQueryPort;
import com.banking.cards_service.application.port.CardChargeRepositoryPort;
import com.banking.cards_service.domain.model.CardCharge;
import com.banking.cards_service.domain.model.CardChargeStatus;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CardChargeJpaAdapter implements CardChargeRepositoryPort, CardChargeQueryPort {

    private final CardChargeJpaRepository repo;

    public CardChargeJpaAdapter(CardChargeJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<CardCharge> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey) {
        return repo.findByUserIdAndIdempotencyKey(userId, idempotencyKey).map(this::toDomain);
    }

    @Override
    public CardCharge save(CardCharge charge) {
        CardChargeEntity saved = repo.save(toEntity(charge));
        return toDomain(saved);
    }

    @Override
    public Optional<CardCharge> findById(UUID chargeId) {
        return repo.findById(chargeId).map(this::toDomain);
    }

    @Override
    public List<CardCharge> listByUserId(UUID userId, int limit, int offset) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<CardChargeEntity> pageItems = repo.listByUserId(userId, PageRequest.of(page, safeLimit + offsetInPage));
        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<CardCharge> listByCardId(UUID cardId, int limit, int offset) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<CardChargeEntity> pageItems = repo.listByCardId(cardId, PageRequest.of(page, safeLimit + offsetInPage));
        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long sumAmountSince(UUID cardId, java.util.Collection<String> statuses, long sinceEpochMs) {
        return repo.sumAmountSince(cardId, statuses, sinceEpochMs);
    }

    private CardCharge toDomain(CardChargeEntity e) {
        return new CardCharge(
                e.getId(),
                e.getUserId(),
                e.getCardId(),
                e.getMerchantAccountId(),
                e.getAmountCents(),
                e.getCreatedAtEpochMs(),
                CardChargeStatus.valueOf(e.getStatus()),
                e.getIdempotencyKey(),
                e.getDescription(),
                e.getTransferId(),
                e.getFailureMessage(),
                e.getFeeCents()
        );
    }

    private CardChargeEntity toEntity(CardCharge c) {
        return new CardChargeEntity(
                c.id(),
                c.userId(),
                c.cardId(),
                c.merchantAccountId(),
                c.amountCents(),
                c.createdAtEpochMs(),
                c.status().name(),
                c.idempotencyKey(),
                c.description(),
                c.transferId(),
                c.failureMessage(),
                c.feeCents()
        );
    }
}
