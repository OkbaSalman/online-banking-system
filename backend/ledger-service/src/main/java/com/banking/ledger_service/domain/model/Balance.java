package com.banking.ledger_service.domain.model;

import java.util.UUID;

public record Balance(UUID accountId, long availableCents) {}