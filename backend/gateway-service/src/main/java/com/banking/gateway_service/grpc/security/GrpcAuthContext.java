package com.banking.gateway_service.grpc.security;

import java.util.function.Supplier;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import io.grpc.Context;

public final class GrpcAuthContext {

    public static final Context.Key<String> USER_ID = Context.key("x-user-id");
    public static final Context.Key<String> ROLE = Context.key("x-role");

    private GrpcAuthContext() {}

   public static <T> T callWithAuth(Authentication authentication, Supplier<T> action) {
    if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
        return action.get();
    }

    String userId = jwtAuth.getToken().getSubject();
    String role = jwtAuth.getToken().getClaimAsString("role");

    Context ctx = Context.current()
            .withValue(USER_ID, userId)
            .withValue(ROLE, role);

    java.util.concurrent.atomic.AtomicReference<T> result = new java.util.concurrent.atomic.AtomicReference<>();
    java.util.concurrent.atomic.AtomicReference<RuntimeException> error = new java.util.concurrent.atomic.AtomicReference<>();

    ctx.run(() -> {
        try {
            result.set(action.get());
        } catch (RuntimeException e) {
            error.set(e);
        }
    });

    if (error.get() != null) {
        throw error.get();
    }
    return result.get();
}
}