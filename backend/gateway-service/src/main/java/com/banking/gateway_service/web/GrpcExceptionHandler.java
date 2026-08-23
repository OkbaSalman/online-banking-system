package com.banking.gateway_service.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

@RestControllerAdvice
public class GrpcExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ProblemDetail> handle(StatusRuntimeException ex) {
        Status.Code code = ex.getStatus().getCode();
        HttpStatus httpStatus = switch (code) {
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case FAILED_PRECONDITION -> HttpStatus.BAD_REQUEST;
            case OUT_OF_RANGE -> HttpStatus.BAD_REQUEST;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case ABORTED -> HttpStatus.CONFLICT;
            case RESOURCE_EXHAUSTED -> HttpStatus.TOO_MANY_REQUESTS;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        String detail = ex.getStatus().getDescription();
        if (detail == null || detail.isBlank()) {
            detail = ex.getMessage();
        }

        ProblemDetail body = ProblemDetail.forStatusAndDetail(httpStatus, detail == null ? "" : detail);
        body.setTitle(code.name());
        body.setProperty("grpcCode", code.name());

        return ResponseEntity.status(httpStatus).body(body);
    }
}