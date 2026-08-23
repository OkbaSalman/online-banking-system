package com.banking.billing_service.adapter.in.grpc;

import com.banking.billing_service.application.usecase.common.exception.NotFoundException;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;

public final class GrpcErrorMapper {

    private GrpcErrorMapper() {}

    public static Status toStatus(Throwable t) {
        if (t instanceof StatusRuntimeException sre) {
            return sre.getStatus();
        }
        if (t instanceof StatusException se) {
            return se.getStatus();
        }
        if (t instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT.withDescription(t.getMessage());
        }
        if (t instanceof NotFoundException) {
            return Status.NOT_FOUND.withDescription(t.getMessage());
        }
        return Status.INTERNAL.withDescription("Internal error");
    }
}
