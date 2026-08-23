package com.banking.ledger_service.application.usecase.get_my_chain_head.dto;

import java.util.UUID;

public record GetMyChainHeadQuery(UUID accountId) {}