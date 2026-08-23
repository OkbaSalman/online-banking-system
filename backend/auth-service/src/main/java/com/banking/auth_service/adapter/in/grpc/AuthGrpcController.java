package com.banking.auth_service.adapter.in.grpc;

import org.springframework.stereotype.Service;

import com.banking.auth.v1.AuthServiceGrpc.AuthServiceImplBase;
import com.banking.auth.v1.PingRequest;
import com.banking.auth.v1.PingResponse;
import com.banking.auth_service.application.usecase.register.RegisterUserUseCase;
import com.banking.auth_service.application.usecase.register.dto.RegisterUserCommand;

import com.banking.auth.v1.VerifyEmailRequest;
import com.banking.auth.v1.VerifyEmailResponse;
import com.banking.auth_service.application.usecase.verify_email.VerifyEmailUseCase;
import com.banking.auth_service.application.usecase.verify_email.dto.VerifyEmailCommand;

import com.banking.auth.v1.LoginRequest;
import com.banking.auth.v1.LoginResponse;
import com.banking.auth_service.application.usecase.login.LoginUseCase;
import com.banking.auth_service.application.usecase.login.dto.LoginCommand;

import com.banking.auth.v1.RefreshRequest;
import com.banking.auth.v1.RefreshResponse;
import com.banking.auth_service.application.usecase.refresh.RefreshUseCase;
import com.banking.auth_service.application.usecase.refresh.dto.RefreshCommand;

import com.banking.auth.v1.LogoutRequest;
import com.banking.auth.v1.LogoutResponse;
import com.banking.auth_service.application.usecase.logout.LogoutUseCase;
import com.banking.auth_service.application.usecase.logout.dto.LogoutCommand;

import com.banking.auth.v1.GetUserRequest;
import com.banking.auth.v1.GetUserResponse;
import com.banking.auth.v1.RequestPasswordResetRequest;
import com.banking.auth.v1.RequestPasswordResetResponse;
import com.banking.auth.v1.ResetPasswordRequest;
import com.banking.auth.v1.ResetPasswordResponse;
import com.banking.auth.v1.ResendVerificationCodeRequest;
import com.banking.auth.v1.ResendVerificationCodeResponse;
import com.banking.auth.v1.SearchUsersRequest;
import com.banking.auth.v1.SearchUsersResponse;
import com.banking.auth.v1.UserSummary;
import com.banking.auth.v1.AdminSetUserBlockedRequest;
import com.banking.auth.v1.AdminSetUserBlockedResponse;
import com.banking.auth_service.application.port.UserRepositoryPort;

import com.banking.auth_service.application.usecase.request_password_reset.RequestPasswordResetUseCase;
import com.banking.auth_service.application.usecase.request_password_reset.dto.RequestPasswordResetCommand;
import com.banking.auth_service.application.usecase.reset_password.ResetPasswordUseCase;
import com.banking.auth_service.application.usecase.reset_password.dto.ResetPasswordCommand;
import com.banking.auth_service.application.usecase.resend_verification_code.ResendVerificationCodeUseCase;
import com.banking.auth_service.application.usecase.resend_verification_code.dto.ResendVerificationCodeCommand;
import com.banking.auth_service.application.usecase.search_users.SearchUsersUseCase;
import com.banking.auth_service.application.usecase.search_users.dto.SearchUsersQuery;
import com.banking.auth_service.application.usecase.set_user_blocked.SetUserBlockedUseCase;
import com.banking.auth_service.application.usecase.set_user_blocked.dto.SetUserBlockedCommand;
import com.banking.auth_service.application.usecase.common.exception.ForbiddenException;
import com.banking.auth_service.application.usecase.common.exception.NotFoundException;
import com.banking.auth_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;

import io.grpc.stub.StreamObserver;
import io.grpc.Status;

@Service
public class AuthGrpcController extends AuthServiceImplBase {

    private final RegisterUserUseCase registerUserUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final UserRepositoryPort users;

    private final ResendVerificationCodeUseCase resendVerificationCodeUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final SearchUsersUseCase searchUsersUseCase;
    private final SetUserBlockedUseCase setUserBlockedUseCase;

    public AuthGrpcController(
            RegisterUserUseCase registerUserUseCase,
            VerifyEmailUseCase verifyEmailUseCase,
            LoginUseCase loginUseCase,
            RefreshUseCase refreshUseCase,
            LogoutUseCase logoutUseCase,
            UserRepositoryPort users,
            ResendVerificationCodeUseCase resendVerificationCodeUseCase,
            RequestPasswordResetUseCase requestPasswordResetUseCase,
            ResetPasswordUseCase resetPasswordUseCase,
            SearchUsersUseCase searchUsersUseCase,
            SetUserBlockedUseCase setUserBlockedUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshUseCase = refreshUseCase;
        this.logoutUseCase = logoutUseCase;
        this.users = users;

        this.resendVerificationCodeUseCase = resendVerificationCodeUseCase;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.searchUsersUseCase = searchUsersUseCase;
        this.setUserBlockedUseCase = setUserBlockedUseCase;
    }

    private static String currentUserIdRaw() {
        return AuthMetadataServerInterceptor.USER_ID.get();
    }

    private static boolean isAdmin() {
        String role = AuthMetadataServerInterceptor.ROLE.get();
        return "ADMIN".equals(role);
    }

    private static void requireAdmin() {
        if (!isAdmin()) {
            throw Status.PERMISSION_DENIED.withDescription("ADMIN role required").asRuntimeException();
        }
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        PingResponse response = PingResponse.newBuilder()
                .setMessage("pong: " + request.getMessage())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void register(com.banking.auth.v1.RegisterRequest request,
                     StreamObserver<com.banking.auth.v1.RegisterResponse> responseObserver) {
        try {
            var result = registerUserUseCase.register(
                    new RegisterUserCommand(request.getEmail(), request.getPassword())
            );

            var response = com.banking.auth.v1.RegisterResponse.newBuilder()
                    .setUserId(result.userId().toString())
                    .setVerificationRequired(result.verificationRequired())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.ALREADY_EXISTS.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Registration failed").withCause(ex).asRuntimeException()
            );
        }
    }

    @Override
    public void verifyEmail(VerifyEmailRequest request, StreamObserver<VerifyEmailResponse> responseObserver) {
        try {
            var result = verifyEmailUseCase.verify(new VerifyEmailCommand(request.getEmail(), request.getCode()));

            var response = VerifyEmailResponse.newBuilder()
                    .setVerified(result.verified())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("VerifyEmail failed").withCause(ex).asRuntimeException()
            );
        }
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            var result = loginUseCase.login(new LoginCommand(request.getEmail(), request.getPassword()));

            var response = LoginResponse.newBuilder()
                    .setVerificationRequired(result.verificationRequired())
                    .setAccessToken(result.accessToken() == null ? "" : result.accessToken())
                    .setAccessExpiresInSeconds(result.accessExpiresInSeconds())
                    .setRefreshToken(result.refreshToken() == null ? "" : result.refreshToken())
                    .setRefreshExpiresInSeconds(result.refreshExpiresInSeconds())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ForbiddenException ex) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (IllegalArgumentException ex) {
            if ("Invalid email or password".equals(ex.getMessage())) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(ex.getMessage()).asRuntimeException());
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException());
            }
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Login failed").withCause(ex).asRuntimeException()
            );
        }
    }

    @Override
    public void refresh(RefreshRequest request, StreamObserver<RefreshResponse> responseObserver) {
        try {
            var result = refreshUseCase.refresh(new RefreshCommand(request.getRefreshToken()));

            var response = RefreshResponse.newBuilder()
                    .setAccessToken(result.accessToken() == null ? "" : result.accessToken())
                    .setAccessExpiresInSeconds(result.accessExpiresInSeconds())
                    .setRefreshToken(result.refreshToken() == null ? "" : result.refreshToken())
                    .setRefreshExpiresInSeconds(result.refreshExpiresInSeconds())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ForbiddenException ex) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            if ("Invalid refresh token".equals(msg) || "User not found".equals(msg)) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(msg).asRuntimeException());
            }
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Refresh failed").withCause(ex).asRuntimeException()
            );
        }
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        try {
            var result = logoutUseCase.logout(new LogoutCommand(request.getRefreshToken()));

            var response = LogoutResponse.newBuilder()
                    .setSuccess(result.success())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Logout failed").withCause(ex).asRuntimeException()
            );
        }
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        try {
            if (request.getUserId() == null || request.getUserId().isBlank()) {
                throw Status.INVALID_ARGUMENT.withDescription("user_id is required").asRuntimeException();
            }

            java.util.UUID userId;
            try {
                userId = java.util.UUID.fromString(request.getUserId());
            } catch (IllegalArgumentException ex) {
                throw Status.INVALID_ARGUMENT.withDescription("Invalid user_id").withCause(ex).asRuntimeException();
            }

            String requesterUserIdRaw = currentUserIdRaw();
            if (requesterUserIdRaw == null || requesterUserIdRaw.isBlank()) {
                throw Status.UNAUTHENTICATED.withDescription("Missing x-user-id").asRuntimeException();
            }

            if (!isAdmin() && !requesterUserIdRaw.equals(userId.toString())) {
                throw Status.PERMISSION_DENIED.withDescription("Not allowed").asRuntimeException();
            }

            var user = users.findById(userId)
                    .orElseThrow(() -> Status.NOT_FOUND.withDescription("User not found").asRuntimeException());

            var response = GetUserResponse.newBuilder()
                    .setUserId(user.id().toString())
                    .setEmail(user.email())
                    .setRole(user.role().name())
                    .setEmailVerified(user.emailVerified())
                    .setBlocked(user.blocked())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (io.grpc.StatusRuntimeException ex) {
            responseObserver.onError(ex);
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("GetUser failed").withCause(ex).asRuntimeException()
            );
        }
    }

    @Override
    public void adminSetUserBlocked(AdminSetUserBlockedRequest request, StreamObserver<AdminSetUserBlockedResponse> responseObserver) {
        try {
            requireAdmin();

            if (request.getUserId() == null || request.getUserId().isBlank()) {
                throw Status.INVALID_ARGUMENT.withDescription("user_id is required").asRuntimeException();
            }

            java.util.UUID userId;
            try {
                userId = java.util.UUID.fromString(request.getUserId());
            } catch (IllegalArgumentException ex) {
                throw Status.INVALID_ARGUMENT.withDescription("Invalid user_id").withCause(ex).asRuntimeException();
            }

            String requesterUserIdRaw = currentUserIdRaw();
            if (requesterUserIdRaw == null || requesterUserIdRaw.isBlank()) {
                throw Status.UNAUTHENTICATED.withDescription("Missing x-user-id").asRuntimeException();
            }

            java.util.UUID requesterUserId;
            try {
                requesterUserId = java.util.UUID.fromString(requesterUserIdRaw);
            } catch (IllegalArgumentException ex) {
                throw Status.UNAUTHENTICATED.withDescription("Invalid x-user-id").withCause(ex).asRuntimeException();
            }

            var result = setUserBlockedUseCase.setBlocked(new SetUserBlockedCommand(
                    requesterUserId,
                    true,
                    userId,
                    request.getBlocked()
            ));

            responseObserver.onNext(AdminSetUserBlockedResponse.newBuilder()
                    .setUserId(result.userId().toString())
                    .setBlocked(result.blocked())
                    .build());
            responseObserver.onCompleted();
        } catch (ForbiddenException ex) {
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription(ex.getMessage()).asRuntimeException());
        } catch (NotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        } catch (io.grpc.StatusRuntimeException ex) {
            responseObserver.onError(ex);
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("AdminSetUserBlocked failed").withCause(ex).asRuntimeException()
            );
        }
    }

    @Override
    public void resendVerificationCode(ResendVerificationCodeRequest request, StreamObserver<ResendVerificationCodeResponse> responseObserver) {
        try {
            var result = resendVerificationCodeUseCase.resend(new ResendVerificationCodeCommand(request.getEmail()));

            var response = ResendVerificationCodeResponse.newBuilder()
                    .setSent(result.sent())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("ResendVerificationCode failed").withCause(ex).asRuntimeException()
            );
        }
    }

    @Override
    public void requestPasswordReset(RequestPasswordResetRequest request, StreamObserver<RequestPasswordResetResponse> responseObserver) {
        try {
            var result = requestPasswordResetUseCase.request(new RequestPasswordResetCommand(request.getEmail()));

            var response = RequestPasswordResetResponse.newBuilder()
                    .setRequested(result.requested())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("RequestPasswordReset failed").withCause(ex).asRuntimeException()
            );
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request, StreamObserver<ResetPasswordResponse> responseObserver) {
        try {
            var result = resetPasswordUseCase.reset(new ResetPasswordCommand(request.getToken(), request.getNewPassword()));

            var response = ResetPasswordResponse.newBuilder()
                    .setSuccess(result.success())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("ResetPassword failed").withCause(ex).asRuntimeException()
            );
        }
    }

    @Override
    public void searchUsers(SearchUsersRequest request, StreamObserver<SearchUsersResponse> responseObserver) {
        try {
            var res = searchUsersUseCase.search(new SearchUsersQuery(request.getQuery(), request.getLimit(), request.getOffset()));

            var response = SearchUsersResponse.newBuilder();
            for (var u : res.users()) {
                response.addUsers(UserSummary.newBuilder()
                        .setUserId(u.id().toString())
                        .setEmail(u.email())
                        .setRole(u.role().name())
                        .setEmailVerified(u.emailVerified())
                        .setBlocked(u.blocked())
                        .build());
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException()
            );
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("SearchUsers failed").withCause(ex).asRuntimeException()
            );
        }
    }
}
