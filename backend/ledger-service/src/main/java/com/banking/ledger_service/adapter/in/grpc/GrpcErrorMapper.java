package com.banking.ledger_service.adapter.in.grpc;

import com.banking.ledger_service.application.usecase.common.exception.InsufficientFundsException;
import com.banking.ledger_service.application.usecase.common.exception.NotFoundException;
import io.grpc.Status;

public final class GrpcErrorMapper {

    private GrpcErrorMapper() {}

    public static Status toStatus(Throwable t) {
        if (t instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(t.getMessage());
        }
        if (t instanceof InsufficientFundsException) {
            return Status.FAILED_PRECONDITION.withDescription(t.getMessage());
        }
        if (t instanceof NotFoundException) {
            return Status.NOT_FOUND.withDescription(t.getMessage());
        }
        return Status.INTERNAL.withDescription("Internal error");
    }
}