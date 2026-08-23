package com.banking.kyc_service.adapter.in.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public final class GrpcErrorMapper {
    private GrpcErrorMapper() {}

    public static StatusRuntimeException map(Throwable ex) {
        if (ex instanceof StatusRuntimeException sre) {
            return sre;
        }
        if (ex instanceof IllegalArgumentException iae) {
            return Status.INVALID_ARGUMENT.withDescription(iae.getMessage()).withCause(iae).asRuntimeException();
        }
        if (ex instanceof IllegalStateException ise) {
            return Status.FAILED_PRECONDITION.withDescription(ise.getMessage()).withCause(ise).asRuntimeException();
        }
        return Status.INTERNAL.withDescription("Request failed").withCause(ex).asRuntimeException();
    }
}