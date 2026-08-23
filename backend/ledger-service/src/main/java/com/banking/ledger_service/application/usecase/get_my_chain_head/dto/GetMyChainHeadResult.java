package com.banking.ledger_service.application.usecase.get_my_chain_head.dto;

import java.util.UUID;

public record GetMyChainHeadResult(long headSeq, String headHash, UUID headEntryId) {}