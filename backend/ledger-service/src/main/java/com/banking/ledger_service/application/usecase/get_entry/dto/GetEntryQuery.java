package com.banking.ledger_service.application.usecase.get_entry.dto;

import java.util.UUID;

public record GetEntryQuery(UUID entryId) {}