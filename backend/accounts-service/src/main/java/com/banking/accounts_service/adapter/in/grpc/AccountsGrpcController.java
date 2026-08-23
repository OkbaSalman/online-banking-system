package com.banking.accounts_service.adapter.in.grpc;

import com.banking.accounts.v1.*;
import com.banking.accounts_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import com.banking.accounts_service.application.usecase.add_member.AddMemberUseCase;
import com.banking.accounts_service.application.usecase.add_member.dto.AddMemberCommand;
import com.banking.accounts_service.application.usecase.can_debit.CanDebitUseCase;
import com.banking.accounts_service.application.usecase.can_debit.dto.CanDebitQuery;
import com.banking.accounts_service.application.usecase.create_account.CreateAccountUseCase;
import com.banking.accounts_service.application.usecase.create_account.dto.CreateAccountCommand;
import com.banking.accounts_service.application.usecase.get_account.GetAccountUseCase;
import com.banking.accounts_service.application.usecase.get_account.dto.GetAccountQuery;
import com.banking.accounts_service.application.usecase.invite_member.InviteMemberUseCase;
import com.banking.accounts_service.application.usecase.invite_member.dto.InviteMemberCommand;
import com.banking.accounts_service.application.usecase.list_my_invitations.ListMyInvitationsUseCase;
import com.banking.accounts_service.application.usecase.list_my_invitations.dto.ListMyInvitationsQuery;
import com.banking.accounts_service.application.usecase.accept_invitation.AcceptInvitationUseCase;
import com.banking.accounts_service.application.usecase.accept_invitation.dto.AcceptInvitationCommand;
import com.banking.accounts_service.application.usecase.decline_invitation.DeclineInvitationUseCase;
import com.banking.accounts_service.application.usecase.decline_invitation.dto.DeclineInvitationCommand;
import com.banking.accounts_service.application.usecase.cancel_invitation.CancelInvitationUseCase;
import com.banking.accounts_service.application.usecase.cancel_invitation.dto.CancelInvitationCommand;
import com.banking.accounts_service.application.usecase.is_member.IsMemberUseCase;
import com.banking.accounts_service.application.usecase.is_member.dto.IsMemberQuery;
import com.banking.accounts_service.application.usecase.list_account_invitations.ListAccountInvitationsUseCase;
import com.banking.accounts_service.application.usecase.list_account_invitations.dto.ListAccountInvitationsQuery;
import com.banking.accounts_service.application.usecase.list_account_members.ListAccountMembersUseCase;
import com.banking.accounts_service.application.usecase.list_account_members.dto.ListAccountMembersQuery;
import com.banking.accounts_service.application.usecase.remove_member.RemoveMemberUseCase;
import com.banking.accounts_service.application.usecase.remove_member.dto.RemoveMemberCommand;
import com.banking.accounts_service.application.usecase.set_account_display_name.SetAccountDisplayNameUseCase;
import com.banking.accounts_service.application.usecase.set_account_display_name.dto.SetAccountDisplayNameCommand;
import com.banking.accounts_service.application.usecase.set_account_frozen.SetAccountFrozenUseCase;
import com.banking.accounts_service.application.usecase.set_account_frozen.dto.SetAccountFrozenCommand;
import com.banking.accounts_service.application.usecase.list_accounts_by_type.ListAccountsByTypeUseCase;
import com.banking.accounts_service.application.usecase.list_accounts_by_type.dto.ListAccountsByTypeQuery;
import com.banking.accounts_service.application.usecase.list_my_accounts.ListMyAccountsUseCase;
import com.banking.accounts_service.application.usecase.list_my_accounts.dto.ListMyAccountsQuery;
import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.domain.model.Account;
import com.banking.accounts_service.domain.model.AccountInvitation;
import com.banking.accounts_service.domain.model.AccountInvitationStatus;
import com.banking.accounts_service.domain.model.AccountType;
import com.banking.accounts_service.domain.model.AccountMembership;
import com.banking.accounts_service.domain.model.MembershipRole;
import io.grpc.stub.StreamObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountsGrpcController extends AccountsServiceGrpc.AccountsServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(AccountsGrpcController.class);

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final ListMyAccountsUseCase listMyAccountsUseCase;
    private final ListAccountsByTypeUseCase listAccountsByTypeUseCase;
    private final AddMemberUseCase addMemberUseCase;
    private final InviteMemberUseCase inviteMemberUseCase;
    private final ListMyInvitationsUseCase listMyInvitationsUseCase;
    private final ListAccountInvitationsUseCase listAccountInvitationsUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final DeclineInvitationUseCase declineInvitationUseCase;
    private final CancelInvitationUseCase cancelInvitationUseCase;
    private final IsMemberUseCase isMemberUseCase;
    private final ListAccountMembersUseCase listAccountMembersUseCase;
    private final RemoveMemberUseCase removeMemberUseCase;
    private final CanDebitUseCase canDebitUseCase;
    private final SetAccountFrozenUseCase setAccountFrozenUseCase;
    private final SetAccountDisplayNameUseCase setAccountDisplayNameUseCase;
    private final AccountsRepositoryPort accountsRepository;

    public AccountsGrpcController(
            CreateAccountUseCase createAccountUseCase,
            GetAccountUseCase getAccountUseCase,
            ListMyAccountsUseCase listMyAccountsUseCase,
            ListAccountsByTypeUseCase listAccountsByTypeUseCase,
            AddMemberUseCase addMemberUseCase,
            InviteMemberUseCase inviteMemberUseCase,
            ListMyInvitationsUseCase listMyInvitationsUseCase,
            ListAccountInvitationsUseCase listAccountInvitationsUseCase,
            AcceptInvitationUseCase acceptInvitationUseCase,
            DeclineInvitationUseCase declineInvitationUseCase,
            CancelInvitationUseCase cancelInvitationUseCase,
            IsMemberUseCase isMemberUseCase,
            ListAccountMembersUseCase listAccountMembersUseCase,
            RemoveMemberUseCase removeMemberUseCase,
            CanDebitUseCase canDebitUseCase,
            SetAccountFrozenUseCase setAccountFrozenUseCase,
            SetAccountDisplayNameUseCase setAccountDisplayNameUseCase,
            AccountsRepositoryPort accountsRepository
    ) {
        this.createAccountUseCase = createAccountUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.listMyAccountsUseCase = listMyAccountsUseCase;
        this.listAccountsByTypeUseCase = listAccountsByTypeUseCase;
        this.addMemberUseCase = addMemberUseCase;
        this.inviteMemberUseCase = inviteMemberUseCase;
        this.listMyInvitationsUseCase = listMyInvitationsUseCase;
        this.listAccountInvitationsUseCase = listAccountInvitationsUseCase;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.declineInvitationUseCase = declineInvitationUseCase;
        this.cancelInvitationUseCase = cancelInvitationUseCase;
        this.isMemberUseCase = isMemberUseCase;
        this.listAccountMembersUseCase = listAccountMembersUseCase;
        this.removeMemberUseCase = removeMemberUseCase;
        this.canDebitUseCase = canDebitUseCase;
        this.setAccountFrozenUseCase = setAccountFrozenUseCase;
        this.setAccountDisplayNameUseCase = setAccountDisplayNameUseCase;
        this.accountsRepository = accountsRepository;
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().setMessage("pong: " + request.getMessage()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            var res = createAccountUseCase.create(new CreateAccountCommand(
                    requesterUserId,
                    request.getIdempotencyKey(),
                    toDomainType(request.getAccountType()),
                    request.getDisplayName()
            ));
            responseObserver.onNext(CreateAccountResponse.newBuilder().setAccount(toProto(res.account())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void setAccountDisplayName(
            SetAccountDisplayNameRequest request,
            StreamObserver<SetAccountDisplayNameResponse> responseObserver
    ) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            var res = setAccountDisplayNameUseCase.setDisplayName(new SetAccountDisplayNameCommand(
                    requesterUserId,
                    isAdmin(),
                    UUID.fromString(request.getAccountId()),
                    request.getDisplayName()
            ));
            responseObserver.onNext(SetAccountDisplayNameResponse.newBuilder().setAccount(toProto(res.account())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void adminListAccountsByUser(
            AdminListAccountsByUserRequest request,
            StreamObserver<AdminListAccountsByUserResponse> responseObserver
    ) {
        try {
            requireAdmin();
            UUID userId = UUID.fromString(request.getUserId());
            var b = AdminListAccountsByUserResponse.newBuilder();
            for (Account a : accountsRepository.listAccountsByUserId(userId)) {
                b.addAccounts(toProto(a));
            }
            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void adminSetAccountFrozen(AdminSetAccountFrozenRequest request, StreamObserver<AdminSetAccountFrozenResponse> responseObserver) {
        try {
            requireAdmin();

            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = setAccountFrozenUseCase.setFrozen(new SetAccountFrozenCommand(
                    requesterUserId,
                    true,
                    UUID.fromString(request.getAccountId()),
                    request.getFrozen()
            ));

            responseObserver.onNext(AdminSetAccountFrozenResponse.newBuilder()
                    .setAccountId(res.accountId().toString())
                    .setFrozen(res.frozen())
                    .build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void getAccount(GetAccountRequest request, StreamObserver<GetAccountResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            boolean isAdmin = isAdmin();
            var res = getAccountUseCase.get(new GetAccountQuery(UUID.fromString(request.getAccountId()), requesterUserId, isAdmin));
            responseObserver.onNext(GetAccountResponse.newBuilder().setAccount(toProto(res.account())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listMyAccounts(ListMyAccountsRequest request, StreamObserver<ListMyAccountsResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            var res = listMyAccountsUseCase.list(new ListMyAccountsQuery(requesterUserId));
            var b = ListMyAccountsResponse.newBuilder();
            for (Account a : res.accounts()) {
                b.addAccounts(toProto(a));
            }
            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listAccountsByType(ListAccountsByTypeRequest request, StreamObserver<ListAccountsByTypeResponse> responseObserver) {
        try {
            requireAdmin();

            var res = listAccountsByTypeUseCase.list(new ListAccountsByTypeQuery(
                    toDomainType(request.getAccountType()),
                    request.getLimit(),
                    request.getOffset()
            ));

            var b = ListAccountsByTypeResponse.newBuilder();
            for (Account a : res.accounts()) {
                b.addAccounts(toProto(a));
            }
            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void addMember(AddMemberRequest request, StreamObserver<AddMemberResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            boolean isAdmin = isAdmin();

            MembershipRole role = switch (request.getRole()) {
                case MEMBERSHIP_ROLE_OWNER -> MembershipRole.OWNER;
                case MEMBERSHIP_ROLE_MEMBER, MEMBERSHIP_ROLE_UNSPECIFIED, UNRECOGNIZED -> MembershipRole.MEMBER;
            };

            var res = addMemberUseCase.add(new AddMemberCommand(
                    requesterUserId,
                    isAdmin,
                    UUID.fromString(request.getAccountId()),
                    UUID.fromString(request.getMemberUserId()),
                    role
            ));

            responseObserver.onNext(AddMemberResponse.newBuilder().setMembership(toProto(res.membership())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void inviteMember(InviteMemberRequest request, StreamObserver<InviteMemberResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            boolean isAdmin = isAdmin();

            MembershipRole role = switch (request.getRole()) {
                case MEMBERSHIP_ROLE_OWNER -> MembershipRole.OWNER;
                case MEMBERSHIP_ROLE_MEMBER, MEMBERSHIP_ROLE_UNSPECIFIED, UNRECOGNIZED -> MembershipRole.MEMBER;
            };

            var res = inviteMemberUseCase.invite(new InviteMemberCommand(
                    requesterUserId,
                    isAdmin,
                    UUID.fromString(request.getAccountId()),
                    UUID.fromString(request.getInvitedUserId()),
                    role,
                    request.getTtlSeconds() <= 0 ? null : request.getTtlSeconds(),
                    request.getInvitedByEmail()
            ));

            responseObserver.onNext(InviteMemberResponse.newBuilder().setInvitation(toProto(res.invitation())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listMyInvitations(ListMyInvitationsRequest request, StreamObserver<ListMyInvitationsResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            AccountInvitationStatus status = toDomainInvitationStatus(request.getStatus());
            var res = listMyInvitationsUseCase.list(new ListMyInvitationsQuery(
                    requesterUserId,
                    status,
                    request.getLimit(),
                    request.getOffset()
            ));

            var b = ListMyInvitationsResponse.newBuilder();
            for (AccountInvitation inv : res.invitations()) {
                b.addInvitations(toProto(inv));
            }
            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listAccountInvitations(ListAccountInvitationsRequest request, StreamObserver<ListAccountInvitationsResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            boolean isAdmin = isAdmin();

            AccountInvitationStatus status = toDomainInvitationStatus(request.getStatus());
            var res = listAccountInvitationsUseCase.list(new ListAccountInvitationsQuery(
                    requesterUserId,
                    isAdmin,
                    UUID.fromString(request.getAccountId()),
                    status,
                    request.getLimit(),
                    request.getOffset()
            ));

            var b = ListAccountInvitationsResponse.newBuilder();
            for (AccountInvitation inv : res.invitations()) {
                b.addInvitations(toProto(inv));
            }
            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void acceptInvitation(AcceptInvitationRequest request, StreamObserver<AcceptInvitationResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            var res = acceptInvitationUseCase.accept(new AcceptInvitationCommand(
                    requesterUserId,
                    UUID.fromString(request.getInvitationId())
            ));

            responseObserver.onNext(AcceptInvitationResponse.newBuilder().setMembership(toProto(res.membership())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void declineInvitation(DeclineInvitationRequest request, StreamObserver<DeclineInvitationResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            var res = declineInvitationUseCase.decline(new DeclineInvitationCommand(
                    requesterUserId,
                    UUID.fromString(request.getInvitationId())
            ));

            responseObserver.onNext(DeclineInvitationResponse.newBuilder().setInvitation(toProto(res.invitation())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void cancelInvitation(CancelInvitationRequest request, StreamObserver<CancelInvitationResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            boolean isAdmin = isAdmin();
            var res = cancelInvitationUseCase.cancel(new CancelInvitationCommand(
                    requesterUserId,
                    isAdmin,
                    UUID.fromString(request.getInvitationId())
            ));

            responseObserver.onNext(CancelInvitationResponse.newBuilder().setInvitation(toProto(res.invitation())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void isMember(IsMemberRequest request, StreamObserver<IsMemberResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            boolean isAdmin = isAdmin();

            var res = isMemberUseCase.isMember(new IsMemberQuery(
                    requesterUserId,
                    isAdmin,
                    UUID.fromString(request.getAccountId()),
                    UUID.fromString(request.getUserId())
            ));

            var b = IsMemberResponse.newBuilder().setIsMember(res.isMember());
            if (res.role() != null) {
                b.setRole(toProtoRole(res.role()));
            }
            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listAccountMembers(ListAccountMembersRequest request, StreamObserver<ListAccountMembersResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            boolean isAdmin = isAdmin();
            var res = listAccountMembersUseCase.list(new ListAccountMembersQuery(
                    requesterUserId,
                    isAdmin,
                    UUID.fromString(request.getAccountId())
            ));

            var b = ListAccountMembersResponse.newBuilder();
            for (AccountMembership m : res.members()) {
                b.addMembers(toProto(m));
            }
            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void removeMember(RemoveMemberRequest request, StreamObserver<RemoveMemberResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            boolean isAdmin = isAdmin();
            var res = removeMemberUseCase.remove(new RemoveMemberCommand(
                    requesterUserId,
                    isAdmin,
                    UUID.fromString(request.getAccountId()),
                    UUID.fromString(request.getUserId())
            ));

            responseObserver.onNext(RemoveMemberResponse.newBuilder().setSuccess(res.success()).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            log.error("removeMember failed: accountId={}, userIdToRemove={}", request.getAccountId(), request.getUserId(), t);
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void canDebit(CanDebitRequest request, StreamObserver<CanDebitResponse> responseObserver) {
        try {
            UUID requesterUserId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            boolean isAdmin = isAdmin();

            var res = canDebitUseCase.canDebit(new CanDebitQuery(
                    requesterUserId,
                    isAdmin,
                    UUID.fromString(request.getAccountId()),
                    UUID.fromString(request.getUserId())
            ));

            var b = CanDebitResponse.newBuilder()
                    .setAllowed(res.allowed())
                    .setReason(res.reason() == null ? "" : res.reason());
            if (res.accountType() != null) {
                b.setAccountType(toProtoType(res.accountType()));
            }
            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    private static com.banking.accounts.v1.Account toProto(Account a) {
        return com.banking.accounts.v1.Account.newBuilder()
                .setId(a.id().toString())
                .setIban(a.iban())
                .setCreatedAtEpochMs(a.createdAtEpochMs())
                .setAccountType(toProtoType(a.accountType()))
                .setFrozen(a.frozen())
                .setDisplayName(a.displayName() == null ? "" : a.displayName())
                .build();
    }

    private static com.banking.accounts.v1.AccountMembership toProto(AccountMembership m) {
        return com.banking.accounts.v1.AccountMembership.newBuilder()
                .setAccountId(m.accountId().toString())
                .setUserId(m.userId().toString())
                .setRole(toProtoRole(m.role()))
                .setCreatedAtEpochMs(m.createdAtEpochMs())
                .build();
    }

    private static com.banking.accounts.v1.AccountInvitation toProto(AccountInvitation inv) {
        long respondedAt = inv.respondedAtEpochMs() == null ? 0 : inv.respondedAtEpochMs();
        return com.banking.accounts.v1.AccountInvitation.newBuilder()
                .setId(inv.id().toString())
                .setAccountId(inv.accountId().toString())
                .setInvitedUserId(inv.invitedUserId().toString())
                .setInvitedByUserId(inv.invitedByUserId().toString())
                .setRole(toProtoRole(inv.role()))
                .setStatus(toProtoInvitationStatus(inv.status()))
                .setCreatedAtEpochMs(inv.createdAtEpochMs())
                .setExpiresAtEpochMs(inv.expiresAtEpochMs())
                .setRespondedAtEpochMs(respondedAt)
                .setInvitedByEmail(inv.invitedByEmail() == null ? "" : inv.invitedByEmail())
                .build();
    }

    private static com.banking.accounts.v1.InvitationStatus toProtoInvitationStatus(AccountInvitationStatus status) {
        if (status == null) {
            return com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_UNSPECIFIED;
        }
        return switch (status) {
            case PENDING -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_PENDING;
            case ACCEPTED -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_ACCEPTED;
            case DECLINED -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_DECLINED;
            case EXPIRED -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_EXPIRED;
            case CANCELED -> com.banking.accounts.v1.InvitationStatus.INVITATION_STATUS_CANCELED;
        };
    }

    private static AccountInvitationStatus toDomainInvitationStatus(com.banking.accounts.v1.InvitationStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INVITATION_STATUS_PENDING -> AccountInvitationStatus.PENDING;
            case INVITATION_STATUS_ACCEPTED -> AccountInvitationStatus.ACCEPTED;
            case INVITATION_STATUS_DECLINED -> AccountInvitationStatus.DECLINED;
            case INVITATION_STATUS_EXPIRED -> AccountInvitationStatus.EXPIRED;
            case INVITATION_STATUS_CANCELED -> AccountInvitationStatus.CANCELED;
            case INVITATION_STATUS_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }

    private static com.banking.accounts.v1.MembershipRole toProtoRole(MembershipRole role) {
        if (role == null) {
            return com.banking.accounts.v1.MembershipRole.MEMBERSHIP_ROLE_UNSPECIFIED;
        }
        return switch (role) {
            case OWNER -> com.banking.accounts.v1.MembershipRole.MEMBERSHIP_ROLE_OWNER;
            case MEMBER -> com.banking.accounts.v1.MembershipRole.MEMBERSHIP_ROLE_MEMBER;
        };
    }

    private static com.banking.accounts.v1.AccountType toProtoType(AccountType type) {
        if (type == null) {
            return com.banking.accounts.v1.AccountType.ACCOUNT_TYPE_UNSPECIFIED;
        }
        return switch (type) {
            case CHECKING -> com.banking.accounts.v1.AccountType.ACCOUNT_TYPE_CHECKING;
            case SAVINGS -> com.banking.accounts.v1.AccountType.ACCOUNT_TYPE_SAVINGS;
        };
    }

    private static AccountType toDomainType(com.banking.accounts.v1.AccountType type) {
        if (type == null) {
            return AccountType.CHECKING;
        }
        return switch (type) {
            case ACCOUNT_TYPE_SAVINGS -> AccountType.SAVINGS;
            case ACCOUNT_TYPE_CHECKING, ACCOUNT_TYPE_UNSPECIFIED, UNRECOGNIZED -> AccountType.CHECKING;
        };
    }

    private static void requireAdmin() {
        if (!isAdmin()) {
            throw io.grpc.Status.PERMISSION_DENIED.withDescription("ADMIN role required").asRuntimeException();
        }
    }

    private static boolean isAdmin() {
        String role = AuthMetadataServerInterceptor.ROLE_CTX_KEY.get();
        return "ADMIN".equals(role);
    }
}
