package com.banking.cards_service.application.port;

import com.banking.cards_service.domain.model.Card;

import java.util.List;
import java.util.UUID;

public interface CardQueryPort {
    List<Card> listByUserId(UUID userId, int limit, int offset);
}
