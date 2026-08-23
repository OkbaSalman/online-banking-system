package com.banking.cards_service.application.usecase.list_my_charges.dto;

import java.util.UUID;

public record ListMyChargesQuery(
        UUID userId,
        UUID cardId,
        int limit,
        int offset
) {}
