package com.banking.cards_service.application.port;

import com.banking.cards_service.domain.model.CardCharge;

import java.util.List;
import java.util.UUID;

public interface CardChargeQueryPort {
    List<CardCharge> listByUserId(UUID userId, int limit, int offset);

    List<CardCharge> listByCardId(UUID cardId, int limit, int offset);

    long sumAmountSince(UUID cardId, java.util.Collection<String> statuses, long sinceEpochMs);
}
