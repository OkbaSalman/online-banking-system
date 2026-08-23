package com.banking.ledger_service.application.usecase.get_balance.dto;

import java.util.UUID;

public record GetBalanceResult(UUID accountId, long availableCents) {}