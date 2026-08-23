package com.banking.ledger_service.application.usecase.list_entries.dto;

import java.util.UUID;

public record ListEntriesQuery(UUID accountId, int limit, int offset) {}