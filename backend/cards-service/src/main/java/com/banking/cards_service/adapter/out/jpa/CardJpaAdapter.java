package com.banking.cards_service.adapter.out.jpa;

import com.banking.cards_service.adapter.out.jpa.entity.CardEntity;
import com.banking.cards_service.adapter.out.jpa.repository.CardJpaRepository;
import com.banking.cards_service.application.port.CardQueryPort;
import com.banking.cards_service.application.port.CardRepositoryPort;
import com.banking.cards_service.domain.model.Card;
import com.banking.cards_service.domain.model.CardStatus;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CardJpaAdapter implements CardRepositoryPort, CardQueryPort {

    private final CardJpaRepository repo;

    public CardJpaAdapter(CardJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Card> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey) {
        return repo.findByUserIdAndIdempotencyKey(userId, idempotencyKey).map(this::toDomain);
    }

    @Override
    public Optional<Card> findById(UUID cardId) {
        return repo.findById(cardId).map(this::toDomain);
    }

    @Override
    public Card save(Card card) {
        CardEntity saved = repo.save(toEntity(card));
        return toDomain(saved);
    }

    @Override
    public List<Card> listByUserId(UUID userId, int limit, int offset) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<CardEntity> pageItems = repo.listByUserId(userId, PageRequest.of(page, safeLimit + offsetInPage));
        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    private Card toDomain(CardEntity e) {
        return new Card(
                e.getId(),
                e.getUserId(),
                e.getFundingAccountId(),
                e.getLast4(),
                CardStatus.valueOf(e.getStatus()),
                e.getCreatedAtEpochMs(),
                e.getIdempotencyKey(),
                e.getNickname(),
                e.getDailyLimitCents(),
                e.getMonthlyLimitCents(),
                e.getPerTransactionLimitCents()
        );
    }

    private CardEntity toEntity(Card c) {
        return new CardEntity(
                c.id(),
                c.userId(),
                c.fundingAccountId(),
                c.last4(),
                c.status().name(),
                c.createdAtEpochMs(),
                c.idempotencyKey(),
                c.nickname(),
                c.dailyLimitCents(),
                c.monthlyLimitCents(),
                c.perTransactionLimitCents()
        );
    }
}
