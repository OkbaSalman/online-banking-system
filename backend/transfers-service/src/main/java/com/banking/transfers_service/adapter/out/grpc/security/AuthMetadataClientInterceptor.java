package com.banking.transfers_service.adapter.out.grpc.security;

import com.banking.transfers_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import java.util.UUID;

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
                UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
                String role = AuthMetadataServerInterceptor.ROLE_CTX_KEY.get();

                if (userId != null) {
                    headers.put(USER_ID_HEADER, userId.toString());
                }
                if (role != null && !role.isBlank()) {
                    headers.put(ROLE_HEADER, role);
                }

                super.start(responseListener, headers);
            }
        };
    }
}
