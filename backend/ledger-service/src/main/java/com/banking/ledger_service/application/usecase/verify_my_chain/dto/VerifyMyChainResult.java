package com.banking.ledger_service.application.usecase.verify_my_chain.dto;

public record VerifyMyChainResult(boolean ok, long firstInvalidSeq, String message) {}