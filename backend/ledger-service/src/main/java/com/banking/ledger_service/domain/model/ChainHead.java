package com.banking.ledger_service.domain.model;

import java.util.UUID;

public record ChainHead(UUID accountId, long headSeq, String headHash, UUID headEntryId) {}