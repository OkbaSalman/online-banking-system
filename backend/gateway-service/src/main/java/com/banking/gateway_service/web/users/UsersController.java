package com.banking.gateway_service.web.users;

import com.banking.auth.v1.AuthServiceGrpc;
import com.banking.auth.v1.AdminSetUserBlockedRequest;
import com.banking.auth.v1.SearchUsersRequest;
import com.banking.auth.v1.GetUserRequest;
import com.banking.gateway_service.grpc.security.GrpcAuthContext;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
public class UsersController {

    private final AuthServiceGrpc.AuthServiceBlockingStub auth;

    public record UserHttpDto(
            String userId,
            String email,
            String role,
            boolean emailVerified,
            boolean blocked
    ) {}

    public record SearchUsersHttpResponse(List<UserHttpDto> users) {}

    public record AdminSetUserBlockedHttpResponse(String userId, boolean blocked) {}

    public UsersController(AuthServiceGrpc.AuthServiceBlockingStub auth) {
        this.auth = auth;
    }

    public record SetBlockedHttpRequest(boolean blocked) {}

    @GetMapping("/api/users/search")
    public Mono<SearchUsersHttpResponse> searchUsers(
            Authentication authentication,
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> auth.searchUsers(
                SearchUsersRequest.newBuilder()
                        .setQuery(query == null ? "" : query)
                        .setLimit(limit)
                        .setOffset(offset)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new SearchUsersHttpResponse(
                  res.getUsersList().stream().map(u -> new UserHttpDto(
                          u.getUserId(),
                          u.getEmail(),
                          u.getRole(),
                          u.getEmailVerified(),
                          u.getBlocked()
                  )).toList()
          ));
    }

    @GetMapping("/api/users/{userId}")
    public Mono<UserHttpDto> getUserById(
            Authentication authentication,
            @PathVariable String userId
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> auth.getUser(
                GetUserRequest.newBuilder().setUserId(userId == null ? "" : userId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(u -> new UserHttpDto(
                  u.getUserId(),
                  u.getEmail(),
                  u.getRole(),
                  u.getEmailVerified(),
                  u.getBlocked()
          ));
    }

    @GetMapping("/api/users/admin/{userId}")
    public Mono<UserHttpDto> adminGetUserById(
            Authentication authentication,
            @PathVariable String userId
    ) {
        return getUserById(authentication, userId);
    }

    @PostMapping("/api/users/admin/{userId}/blocked")
    public Mono<AdminSetUserBlockedHttpResponse> adminSetUserBlocked(
            Authentication authentication,
            @PathVariable String userId,
            @RequestBody SetBlockedHttpRequest body
    ) {
        boolean blocked = body != null && body.blocked();

        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> auth.adminSetUserBlocked(
                AdminSetUserBlockedRequest.newBuilder()
                        .setUserId(userId == null ? "" : userId)
                        .setBlocked(blocked)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new AdminSetUserBlockedHttpResponse(res.getUserId(), res.getBlocked()));
    }
}
