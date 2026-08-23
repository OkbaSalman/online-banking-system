package com.banking.ledger_service.application.port;

public record VerifyChainScanResult(boolean ok, long firstInvalidSeq, String message) {}