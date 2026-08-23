package com.banking.cards_service.application.port;

import com.banking.cards_service.domain.model.Card;

import java.util.Optional;
import java.util.UUID;

public interface CardRepositoryPort {
    Optional<Card> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    Optional<Card> findById(UUID cardId);

    Card save(Card card);
}
