package com.banking.ledger_service.application.usecase.common.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}