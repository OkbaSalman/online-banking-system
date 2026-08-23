package com.banking.gateway_service.grpc.security;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class AuthMetadataClientInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> USER_ID_HEADER =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> ROLE_HEADER =
            Metadata.Key.of("x-role", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next
    ) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String userId = GrpcAuthContext.USER_ID.get();
                String role = GrpcAuthContext.ROLE.get();

                if (userId != null && !userId.isBlank()) {
                    headers.put(USER_ID_HEADER, userId);
                }
                if (role != null && !role.isBlank()) {
                    headers.put(ROLE_HEADER, role);
                }

                super.start(responseListener, headers);
            }
        };
    }
}