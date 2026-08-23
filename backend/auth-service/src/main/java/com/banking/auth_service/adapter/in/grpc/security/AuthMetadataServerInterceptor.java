package com.banking.auth_service.adapter.in.grpc.security;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class AuthMetadataServerInterceptor implements ServerInterceptor {

    public static final Context.Key<String> USER_ID = Context.key("x-user-id");
    public static final Context.Key<String> ROLE = Context.key("x-role");

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
        String userId = headers.get(USER_ID_HEADER);
        String role = headers.get(ROLE_HEADER);

        Context ctx = Context.current()
                .withValue(USER_ID, userId == null ? "" : userId)
                .withValue(ROLE, role == null ? "" : role);

        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
