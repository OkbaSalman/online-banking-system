package com.banking.kyc_service.adapter.in.grpc.security;

import io.grpc.*;
import io.grpc.Metadata.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthMetadataServerInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuthMetadataServerInterceptor.class);

    public static final Context.Key<String> USER_ID = Context.key("x-user-id");
    public static final Context.Key<String> ROLE = Context.key("x-role");

    private static final Key<String> USER_ID_HEADER =
            Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Key<String> ROLE_HEADER =
            Key.of("x-role", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String userId = headers.get(USER_ID_HEADER);
        String role = headers.get(ROLE_HEADER);

        log.debug("gRPC metadata received: userId={}, role={}, keys={}", userId, role, headers.keys());

        Context ctx = Context.current()
                .withValue(USER_ID, userId)
                .withValue(ROLE, role);

        return Contexts.interceptCall(ctx, call, headers, next);
    }
}