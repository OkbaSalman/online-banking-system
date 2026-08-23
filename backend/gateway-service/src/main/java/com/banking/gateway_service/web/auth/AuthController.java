package com.banking.gateway_service.web.auth;


import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.banking.auth.v1.AuthServiceGrpc;
import com.banking.auth.v1.GetUserRequest;
import com.banking.auth.v1.LoginRequest;
import com.banking.auth.v1.RefreshRequest;
import com.banking.auth.v1.RegisterRequest;
import com.banking.auth.v1.LogoutRequest;
import com.banking.auth.v1.VerifyEmailRequest;
import com.banking.auth.v1.ResendVerificationCodeRequest;
import com.banking.auth.v1.RequestPasswordResetRequest;
import com.banking.auth.v1.ResetPasswordRequest;
import com.banking.auth.v1.PingRequest;
import com.banking.gateway_service.grpc.security.GrpcAuthContext;
import com.banking.gateway_service.web.auth.dto.login.LoginHttpRequest;
import com.banking.gateway_service.web.auth.dto.login.LoginHttpResponse;
import com.banking.gateway_service.web.auth.dto.logout.LogoutHttpRequest;
import com.banking.gateway_service.web.auth.dto.logout.LogoutHttpResponse;
import com.banking.gateway_service.web.auth.dto.refresh.RefreshHttpRequest;
import com.banking.gateway_service.web.auth.dto.refresh.RefreshHttpResponse;
import com.banking.gateway_service.web.auth.dto.register.RegisterHttpRequest;
import com.banking.gateway_service.web.auth.dto.register.RegisterHttpResponse;
import com.banking.gateway_service.web.auth.dto.verify.VerifyEmailHttpRequest;
import com.banking.gateway_service.web.auth.dto.verify.VerifyEmailHttpResponse;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class AuthController {

    private final AuthServiceGrpc.AuthServiceBlockingStub auth;

    public record PingHttpResponse(String message) {}

    public record EmailHttpRequest(String email) {}

    public record ResendVerificationHttpResponse(boolean sent) {}

    public record ForgotPasswordHttpResponse(boolean requested) {}

    public record ResetPasswordHttpRequest(String token, String newPassword) {}

    public record ResetPasswordHttpResponse(boolean success) {}

    public record MeHttpResponse(
            String userId,
            String email,
            String role,
            boolean emailVerified,
            boolean blocked
    ) {}

    public AuthController(AuthServiceGrpc.AuthServiceBlockingStub auth) {
        this.auth = auth;
    }

    @GetMapping("/api/auth/ping")
    public Mono<PingHttpResponse> ping(@RequestParam(defaultValue = "hello") String message) {
        return Mono.fromCallable(() -> auth.ping(PingRequest.newBuilder().setMessage(message).build()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(res -> new PingHttpResponse(res.getMessage()));
    }

    @PostMapping("/api/auth/register")
    public Mono<RegisterHttpResponse> register(@RequestBody RegisterHttpRequest body) {
        return Mono.fromCallable(() -> auth.register(
                RegisterRequest.newBuilder()
                        .setEmail(body.email())
                        .setPassword(body.password())
                        .build()
        )).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new RegisterHttpResponse(res.getUserId(), res.getVerificationRequired()));
    }

    @PostMapping("/api/auth/verify-email")
    public Mono<VerifyEmailHttpResponse> verifyEmail(@RequestBody VerifyEmailHttpRequest body) {
        return Mono.fromCallable(() -> auth.verifyEmail(
                VerifyEmailRequest.newBuilder()
                        .setEmail(body.email())
                        .setCode(body.code())
                        .build()
        )).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new VerifyEmailHttpResponse(res.getVerified()));
    }

    @PostMapping("/api/auth/resend-verification")
    public Mono<ResendVerificationHttpResponse> resendVerification(@RequestBody EmailHttpRequest body) {
        String email = body == null ? null : body.email();
        return Mono.fromCallable(() -> auth.resendVerificationCode(
                ResendVerificationCodeRequest.newBuilder().setEmail(email == null ? "" : email).build()
        )).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ResendVerificationHttpResponse(res.getSent()));
    }

    @PostMapping("/api/auth/forgot-password")
    public Mono<ForgotPasswordHttpResponse> forgotPassword(@RequestBody EmailHttpRequest body) {
        String email = body == null ? null : body.email();
        return Mono.fromCallable(() -> auth.requestPasswordReset(
                RequestPasswordResetRequest.newBuilder().setEmail(email == null ? "" : email).build()
        )).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ForgotPasswordHttpResponse(res.getRequested()));
    }

    @PostMapping("/api/auth/reset-password")
    public Mono<ResetPasswordHttpResponse> resetPassword(@RequestBody ResetPasswordHttpRequest body) {
        String token = body == null ? null : body.token();
        String newPassword = body == null ? null : body.newPassword();
        return Mono.fromCallable(() -> auth.resetPassword(
                ResetPasswordRequest.newBuilder()
                        .setToken(token == null ? "" : token)
                        .setNewPassword(newPassword == null ? "" : newPassword)
                        .build()
        )).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ResetPasswordHttpResponse(res.getSuccess()));
    }

    @GetMapping("/api/auth/me")
    public Mono<MeHttpResponse> me(Authentication authentication) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> {
            String userId = ((JwtAuthenticationToken) authentication).getToken().getSubject();
            var res = auth.getUser(GetUserRequest.newBuilder().setUserId(userId).build());
            return new MeHttpResponse(
                    res.getUserId(),
                    res.getEmail(),
                    res.getRole(),
                    res.getEmailVerified(),
                    res.getBlocked()
            );
        })).subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/api/auth/login")
    public Mono<LoginHttpResponse> login(@RequestBody LoginHttpRequest body) {
        return Mono.fromCallable(() -> auth.login(
                LoginRequest.newBuilder()
                        .setEmail(body.email())
                        .setPassword(body.password())
                        .build()
        )).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new LoginHttpResponse(
                  res.getVerificationRequired(),
                  res.getAccessToken(),
                  res.getAccessExpiresInSeconds(),
                  res.getRefreshToken(),
                  res.getRefreshExpiresInSeconds()
          ));
    }

    @PostMapping("/api/auth/refresh")
    public Mono<RefreshHttpResponse> refresh(@RequestBody RefreshHttpRequest body) {
        return Mono.fromCallable(() -> auth.refresh(
                RefreshRequest.newBuilder()
                        .setRefreshToken(body.refreshToken())
                        .build()
        )).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new RefreshHttpResponse(
                  res.getAccessToken(),
                  res.getAccessExpiresInSeconds(),
                  res.getRefreshToken(),
                  res.getRefreshExpiresInSeconds()
          ));
    }

    @PostMapping("/api/auth/logout")
    public Mono<LogoutHttpResponse> logout(@RequestBody LogoutHttpRequest body) {
        return Mono.fromCallable(() -> auth.logout(
                LogoutRequest.newBuilder()
                        .setRefreshToken(body.refreshToken())
                        .build()
        )).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new LogoutHttpResponse(res.getSuccess()));
    }
}