package com.banking.billing_service.adapter.in.grpc.security;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

import java.util.UUID;

public class AuthMetadataServerInterceptor implements ServerInterceptor {

    public static final Context.Key<UUID> USER_ID_CTX_KEY = Context.key("x-user-id");
    public static final Context.Key<String> ROLE_CTX_KEY = Context.key("x-role");

    private static final Metadata.Key<String> USER_ID_HEADER =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> ROLE_HEADER =
            Metadata.Key.of("x-role", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        String method = call.getMethodDescriptor() == null ? "" : call.getMethodDescriptor().getFullMethodName();
        if (method != null && method.endsWith("/Ping")) {
            return next.startCall(call, headers);
        }

        String userIdRaw = headers.get(USER_ID_HEADER);
        if (userIdRaw == null || userIdRaw.isBlank()) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing x-user-id"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        UUID userId;
        try {
            userId = UUID.fromString(userIdRaw);
        } catch (IllegalArgumentException e) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid x-user-id"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        String role = headers.get(ROLE_HEADER);

        Context ctx = Context.current()
                .withValue(USER_ID_CTX_KEY, userId)
                .withValue(ROLE_CTX_KEY, role == null ? "" : role);

        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
