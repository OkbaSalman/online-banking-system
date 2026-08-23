package com.banking.gateway_service.web.accounts;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.accounts.v1.AccountType;
import com.banking.accounts.v1.AddMemberRequest;
import com.banking.accounts.v1.AdminListAccountsByUserRequest;
import com.banking.accounts.v1.AdminSetAccountFrozenRequest;
import com.banking.accounts.v1.CancelInvitationRequest;
import com.banking.accounts.v1.CanDebitRequest;
import com.banking.accounts.v1.CreateAccountRequest;
import com.banking.accounts.v1.SetAccountDisplayNameRequest;
import com.banking.accounts.v1.DeclineInvitationRequest;
import com.banking.accounts.v1.GetAccountRequest;
import com.banking.accounts.v1.IsMemberRequest;
import com.banking.accounts.v1.InviteMemberRequest;
import com.banking.accounts.v1.ListAccountInvitationsRequest;
import com.banking.accounts.v1.ListAccountMembersRequest;
import com.banking.accounts.v1.ListAccountsByTypeRequest;
import com.banking.accounts.v1.ListMyAccountsRequest;
import com.banking.accounts.v1.ListMyInvitationsRequest;
import com.banking.accounts.v1.MembershipRole;
import com.banking.accounts.v1.RemoveMemberRequest;
import com.banking.accounts.v1.AcceptInvitationRequest;
import com.banking.accounts.v1.AccountsServiceGrpc;
import com.banking.accounts.v1.PingRequest;
import com.banking.auth.v1.AuthServiceGrpc;
import com.banking.auth.v1.GetUserRequest;
import com.banking.gateway_service.grpc.security.GrpcAuthContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import com.banking.gateway_service.web.accounts.dto.account.AccountHttpDto;
import com.banking.gateway_service.web.accounts.dto.account.SetAccountDisplayNameHttpRequest;
import com.banking.gateway_service.web.accounts.dto.candebit.CanDebitHttpResponse;
import com.banking.gateway_service.web.accounts.dto.create.CreateAccountHttpRequest;
import com.banking.gateway_service.web.accounts.dto.create.CreateAccountHttpResponse;
import com.banking.gateway_service.web.accounts.dto.get.GetAccountHttpResponse;
import com.banking.gateway_service.web.accounts.dto.list.ListAccountsByTypeHttpResponse;
import com.banking.gateway_service.web.accounts.dto.list.ListMyAccountsHttpResponse;
import com.banking.gateway_service.web.accounts.dto.member.AddMemberHttpRequest;
import com.banking.gateway_service.web.accounts.dto.member.AddMemberHttpResponse;
import com.banking.gateway_service.web.accounts.dto.member.IsMemberHttpResponse;
import com.banking.gateway_service.web.accounts.dto.membership.AccountMembershipHttpDto;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class AccountsController {

    private final AccountsServiceGrpc.AccountsServiceBlockingStub accounts;
    private final AuthServiceGrpc.AuthServiceBlockingStub auth;

    public AccountsController(
            AccountsServiceGrpc.AccountsServiceBlockingStub accounts,
            AuthServiceGrpc.AuthServiceBlockingStub auth
    ) {
        this.accounts = accounts;
        this.auth = auth;
    }

    public record PingHttpResponse(String message) {}

    public record ListAccountMembersHttpResponse(List<AccountMembershipHttpDto> members) {}

    public record RemoveMemberHttpResponse(boolean success) {}

    public record AdminSetFrozenHttpResponse(String accountId, boolean frozen) {}

    public record InviteMemberHttpRequest(String invitedUserId, String role, Long ttlSeconds) {}

    public record InvitationHttpDto(
            String id,
            String accountId,
            String invitedUserId,
            String invitedByUserId,
            String invitedByEmail,
            String role,
            String status,
            long createdAtEpochMs,
            long expiresAtEpochMs,
            long respondedAtEpochMs
    ) {}

    public record ListInvitationsHttpResponse(List<InvitationHttpDto> invitations) {}

    public record GetAccountFullHttpResponse(AccountHttpDto account, List<AccountMembershipHttpDto> members) {}

    public record SetFrozenHttpRequest(boolean frozen) {}

    @GetMapping("/api/accounts/ping")
    public Mono<PingHttpResponse> ping(Authentication authentication, @RequestParam(defaultValue = "hello") String message) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.ping(PingRequest.newBuilder().setMessage(message).build())))
                .subscribeOn(Schedulers.boundedElastic())
                .map(res -> new PingHttpResponse(res.getMessage()));
    }

    @PostMapping("/api/accounts")
    public Mono<CreateAccountHttpResponse> createAccount(Authentication authentication, @RequestBody CreateAccountHttpRequest body) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.createAccount(
                CreateAccountRequest.newBuilder()
                        .setIdempotencyKey(body.idempotencyKey())
                        .setAccountType(parseAccountType(body.accountType()))
                        .setDisplayName(body.displayName() == null ? "" : body.displayName())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new CreateAccountHttpResponse(toHttp(res.getAccount())));
    }

    @PatchMapping("/api/accounts/{accountId}/display-name")
    public Mono<GetAccountHttpResponse> setAccountDisplayName(
            Authentication authentication,
            @PathVariable String accountId,
            @RequestBody SetAccountDisplayNameHttpRequest body
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.setAccountDisplayName(
                SetAccountDisplayNameRequest.newBuilder()
                        .setAccountId(accountId)
                        .setDisplayName(body == null || body.displayName() == null ? "" : body.displayName())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new GetAccountHttpResponse(toHttp(res.getAccount())));
    }

    @GetMapping("/api/accounts/admin/by-user/{userId}")
    public Mono<ListMyAccountsHttpResponse> adminListAccountsByUser(
            Authentication authentication,
            @PathVariable String userId
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.adminListAccountsByUser(
                AdminListAccountsByUserRequest.newBuilder().setUserId(userId == null ? "" : userId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListMyAccountsHttpResponse(res.getAccountsList().stream().map(AccountsController::toHttp).toList()));
    }

    @GetMapping("/api/accounts/{accountId}")
    public Mono<GetAccountHttpResponse> getAccount(Authentication authentication, @PathVariable String accountId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.getAccount(
                GetAccountRequest.newBuilder().setAccountId(accountId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new GetAccountHttpResponse(toHttp(res.getAccount())));
    }

    @GetMapping("/api/accounts")
    public Mono<ListMyAccountsHttpResponse> listMyAccounts(Authentication authentication) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.listMyAccounts(
                ListMyAccountsRequest.newBuilder().build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListMyAccountsHttpResponse(res.getAccountsList().stream().map(AccountsController::toHttp).toList()));
    }

    @GetMapping("/api/accounts/admin/by-type")
    public Mono<ListAccountsByTypeHttpResponse> listAccountsByType(
            Authentication authentication,
            @RequestParam String accountType,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.listAccountsByType(
                ListAccountsByTypeRequest.newBuilder()
                        .setAccountType(parseAccountType(accountType))
                        .setLimit(limit)
                        .setOffset(offset)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListAccountsByTypeHttpResponse(res.getAccountsList().stream().map(AccountsController::toHttp).toList()));
    }

    @PostMapping("/api/accounts/{accountId}/members")
    public Mono<AddMemberHttpResponse> addMember(
            Authentication authentication,
            @PathVariable String accountId,
            @RequestBody AddMemberHttpRequest body
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.addMember(
                AddMemberRequest.newBuilder()
                        .setAccountId(accountId)
                        .setMemberUserId(body.memberUserId())
                        .setRole(parseMembershipRole(body.role()))
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new AddMemberHttpResponse(toHttp(res.getMembership())));
    }

    @GetMapping("/api/accounts/{accountId}/members/{userId}")
    public Mono<IsMemberHttpResponse> isMember(
            Authentication authentication,
            @PathVariable String accountId,
            @PathVariable String userId
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.isMember(
                IsMemberRequest.newBuilder()
                        .setAccountId(accountId)
                        .setUserId(userId)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new IsMemberHttpResponse(res.getIsMember(), res.getRole().name()));
    }

    @GetMapping("/api/accounts/{accountId}/members")
    public Mono<ListAccountMembersHttpResponse> listAccountMembers(Authentication authentication, @PathVariable String accountId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.listAccountMembers(
                ListAccountMembersRequest.newBuilder().setAccountId(accountId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListAccountMembersHttpResponse(res.getMembersList().stream().map(AccountsController::toHttp).toList()));
    }

    @DeleteMapping("/api/accounts/{accountId}/members/{userId}")
    public Mono<RemoveMemberHttpResponse> removeMember(
            Authentication authentication,
            @PathVariable String accountId,
            @PathVariable String userId
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.removeMember(
                RemoveMemberRequest.newBuilder().setAccountId(accountId).setUserId(userId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new RemoveMemberHttpResponse(res.getSuccess()));
    }

    @GetMapping("/api/accounts/{accountId}/can-debit")
    public Mono<CanDebitHttpResponse> canDebit(
            Authentication authentication,
            @PathVariable String accountId,
            @RequestParam String userId
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.canDebit(
                CanDebitRequest.newBuilder()
                        .setAccountId(accountId)
                        .setUserId(userId)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new CanDebitHttpResponse(res.getAllowed(), res.getReason(), res.getAccountType().name()));
    }

    @PostMapping("/api/accounts/admin/{accountId}/frozen")
    public Mono<AdminSetFrozenHttpResponse> adminSetFrozen(
            Authentication authentication,
            @PathVariable String accountId,
            @RequestBody SetFrozenHttpRequest body
    ) {
        boolean frozen = body != null && body.frozen();

        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.adminSetAccountFrozen(
                AdminSetAccountFrozenRequest.newBuilder()
                        .setAccountId(accountId)
                        .setFrozen(frozen)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new AdminSetFrozenHttpResponse(res.getAccountId(), res.getFrozen()));
    }

    @PostMapping("/api/accounts/{accountId}/invitations")
    public Mono<InvitationHttpDto> inviteMember(
            Authentication authentication,
            @PathVariable String accountId,
            @RequestBody InviteMemberHttpRequest body
    ) {
        String invitedUserId = body == null ? null : body.invitedUserId();
        String role = body == null ? null : body.role();
        long ttl = body == null || body.ttlSeconds() == null ? 0 : body.ttlSeconds();

        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> {
            String requesterUserId = authentication instanceof JwtAuthenticationToken jwt
                    ? jwt.getToken().getSubject()
                    : "";
            String inviterEmail = "";
            if (requesterUserId != null && !requesterUserId.isBlank()) {
                try {
                    inviterEmail = auth.getUser(GetUserRequest.newBuilder().setUserId(requesterUserId).build()).getEmail();
                } catch (RuntimeException ignored) {
                    inviterEmail = "";
                }
            }
            return accounts.inviteMember(
                    InviteMemberRequest.newBuilder()
                            .setAccountId(accountId)
                            .setInvitedUserId(invitedUserId == null ? "" : invitedUserId)
                            .setRole(parseMembershipRole(role))
                            .setTtlSeconds(ttl)
                            .setInvitedByEmail(inviterEmail == null ? "" : inviterEmail)
                            .build()
            );
        })).subscribeOn(Schedulers.boundedElastic())
          .map(res -> toHttp(res.getInvitation()));
    }

    @GetMapping("/api/accounts/{accountId}/invitations")
    public Mono<ListInvitationsHttpResponse> listAccountInvitations(
            Authentication authentication,
            @PathVariable String accountId,
            @RequestParam(defaultValue = "INVITATION_STATUS_PENDING") String status,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.listAccountInvitations(
                ListAccountInvitationsRequest.newBuilder()
                        .setAccountId(accountId)
                        .setStatus(parseInvitationStatus(status))
                        .setLimit(limit)
                        .setOffset(offset)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListInvitationsHttpResponse(res.getInvitationsList().stream().map(AccountsController::toHttp).toList()));
    }

    @DeleteMapping("/api/accounts/invitations/{invitationId}")
    public Mono<InvitationHttpDto> cancelInvitation(Authentication authentication, @PathVariable String invitationId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.cancelInvitation(
                CancelInvitationRequest.newBuilder().setInvitationId(invitationId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> toHttp(res.getInvitation()));
    }

    @GetMapping("/api/accounts/{accountId}/full")
    public Mono<GetAccountFullHttpResponse> getAccountFull(Authentication authentication, @PathVariable String accountId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> {
            var accountRes = accounts.getAccount(GetAccountRequest.newBuilder().setAccountId(accountId).build());
            var membersRes = accounts.listAccountMembers(ListAccountMembersRequest.newBuilder().setAccountId(accountId).build());
            return new GetAccountFullHttpResponse(
                    toHttp(accountRes.getAccount()),
                    membersRes.getMembersList().stream().map(AccountsController::toHttp).toList()
            );
        })).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/api/accounts/invitations")
    public Mono<ListInvitationsHttpResponse> listMyInvitations(
            Authentication authentication,
            @RequestParam(defaultValue = "INVITATION_STATUS_PENDING") String status,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.listMyInvitations(
                ListMyInvitationsRequest.newBuilder()
                        .setStatus(parseInvitationStatus(status))
                        .setLimit(limit)
                        .setOffset(offset)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListInvitationsHttpResponse(res.getInvitationsList().stream().map(AccountsController::toHttp).toList()));
    }

    @PostMapping("/api/accounts/invitations/{invitationId}/accept")
    public Mono<AccountMembershipHttpDto> acceptInvitation(
            Authentication authentication,
            @PathVariable String invitationId
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.acceptInvitation(
                AcceptInvitationRequest.newBuilder().setInvitationId(invitationId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> toHttp(res.getMembership()));
    }

    @PostMapping("/api/accounts/invitations/{invitationId}/decline")
    public Mono<InvitationHttpDto> declineInvitation(
            Authentication authentication,
            @PathVariable String invitationId
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> accounts.declineInvitation(
                DeclineInvitationRequest.newBuilder().setInvitationId(invitationId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> toHttp(res.getInvitation()));
    }

    private static InvitationHttpDto toHttp(com.banking.accounts.v1.AccountInvitation inv) {
        return new InvitationHttpDto(
                inv.getId(),
                inv.getAccountId(),
                inv.getInvitedUserId(),
                inv.getInvitedByUserId(),
                inv.getInvitedByEmail(),
                inv.getRole().name(),
                inv.getStatus().name(),
                inv.getCreatedAtEpochMs(),
                inv.getExpiresAtEpochMs(),
                inv.getRespondedAtEpochMs()
        );
    }

    private static AccountType parseAccountType(String accountType) {
        if (accountType == null) {
            return AccountType.ACCOUNT_TYPE_UNSPECIFIED;
        }
        String v = accountType.trim().toUpperCase();
        return switch (v) {
            case "CHECKING", "ACCOUNT_TYPE_CHECKING" -> AccountType.ACCOUNT_TYPE_CHECKING;
            case "SAVINGS", "ACCOUNT_TYPE_SAVINGS" -> AccountType.ACCOUNT_TYPE_SAVINGS;
            default -> AccountType.ACCOUNT_TYPE_UNSPECIFIED;
        };
    }

    private static MembershipRole parseMembershipRole(String role) {
        if (role == null) {
            return MembershipRole.MEMBERSHIP_ROLE_UNSPECIFIED;
        }
        String v = role.trim().toUpperCase();
        return switch (v) {
            case "OWNER", "MEMBERSHIP_ROLE_OWNER" -> MembershipRole.MEMBERSHIP_ROLE_OWNER;
            case "MEMBER", "MEMBERSHIP_ROLE_MEMBER" -> MembershipRole.MEMBERSHIP_ROLE_MEMBER;
            default -> MembershipRole.MEMBERSHIP_ROLE_UNSPECIFIED;
        };
    }

    private static com.banking.accounts.v1.InvitationStatus parseInvitationStatus(String status) {
        if (status == null) {
            return com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_UNSPECIFIED;
        }
        String v = status.trim().toUpperCase();
        return switch (v) {
            case "PENDING", "INVITATION_STATUS_PENDING" -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_PENDING;
            case "ACCEPTED", "INVITATION_STATUS_ACCEPTED" -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_ACCEPTED;
            case "DECLINED", "INVITATION_STATUS_DECLINED" -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_DECLINED;
            case "EXPIRED", "INVITATION_STATUS_EXPIRED" -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_EXPIRED;
            case "CANCELED", "INVITATION_STATUS_CANCELED" -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_CANCELED;
            default -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_UNSPECIFIED;
        };
    }

    private static AccountHttpDto toHttp(com.banking.accounts.v1.Account a) {
        return new AccountHttpDto(
                a.getId(),
                a.getIban(),
                a.getCreatedAtEpochMs(),
                a.getAccountType().name(),
                a.getFrozen(),
                a.getDisplayName()
        );
    }

    private static AccountMembershipHttpDto toHttp(com.banking.accounts.v1.AccountMembership m) {
        return new AccountMembershipHttpDto(m.getAccountId(), m.getUserId(), m.getRole().name(), m.getCreatedAtEpochMs());
    }
}