package com.banking.ledger_service.domain.model;

import java.util.UUID;

public record Posting(UUID accountId, long amountCents) {}