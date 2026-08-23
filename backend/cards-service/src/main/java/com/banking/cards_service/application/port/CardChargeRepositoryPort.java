package com.banking.cards_service.application.port;

import com.banking.cards_service.domain.model.CardCharge;

import java.util.Optional;
import java.util.UUID;

public interface CardChargeRepositoryPort {
    Optional<CardCharge> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);

    CardCharge save(CardCharge charge);

    Optional<CardCharge> findById(UUID chargeId);
}
