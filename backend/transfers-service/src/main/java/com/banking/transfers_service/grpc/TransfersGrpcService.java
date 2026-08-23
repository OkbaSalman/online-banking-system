package com.banking.transfers_service.grpc;

import com.banking.transfers.v1.*;
import com.banking.transfers_service.adapter.in.grpc.GrpcErrorMapper;
import com.banking.transfers_service.grpc.security.AuthMetadataServerInterceptor;
import com.banking.transfers_service.application.usecase.admin_mint.AdminMintUseCase;
import com.banking.transfers_service.application.usecase.admin_mint.dto.AdminMintCommand;
import com.banking.transfers_service.application.usecase.create_transfer.CreateTransferUseCase;
import com.banking.transfers_service.application.usecase.create_transfer.dto.CreateTransferCommand;
import com.banking.transfers_service.application.usecase.get_transfer.GetTransferUseCase;
import com.banking.transfers_service.application.usecase.get_transfer.dto.GetTransferQuery;
import com.banking.transfers_service.application.usecase.list_my_transfers.ListMyTransfersUseCase;
import com.banking.transfers_service.application.usecase.list_my_transfers.dto.ListMyTransfersQuery;
import com.banking.transfers_service.domain.model.Transfer;
import com.banking.transfers_service.domain.model.TransferStatus;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

public class TransfersGrpcService extends TransfersServiceGrpc.TransfersServiceImplBase {

    private final CreateTransferUseCase createTransferUseCase;
    private final GetTransferUseCase getTransferUseCase;
    private final ListMyTransfersUseCase listMyTransfersUseCase;
    private final AdminMintUseCase adminMintUseCase;

    public TransfersGrpcService(
            CreateTransferUseCase createTransferUseCase,
            GetTransferUseCase getTransferUseCase,
            ListMyTransfersUseCase listMyTransfersUseCase,
            AdminMintUseCase adminMintUseCase
    ) {
        this.createTransferUseCase = createTransferUseCase;
        this.getTransferUseCase = getTransferUseCase;
        this.listMyTransfersUseCase = listMyTransfersUseCase;
        this.adminMintUseCase = adminMintUseCase;
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().setMessage("pong: " + request.getMessage()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void createTransfer(CreateTransferRequest request, StreamObserver<CreateTransferResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            var res = createTransferUseCase.create(new CreateTransferCommand(
                    userId,
                    UUID.fromString(request.getFromAccountId()),
                    UUID.fromString(request.getToAccountId()),
                    request.getAmountCents(),
                    request.getIdempotencyKey(),
                    request.getDescription()
            ));

            var b = CreateTransferResponse.newBuilder()
                    .setTransfer(toProto(res.transfer()))
                    .setFromBalanceCents(res.fromBalanceCents())
                    .setToBalanceCents(res.toBalanceCents());

            if (res.ledgerEntry() != null) {
                b.setEntry(res.ledgerEntry());
            }

            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void getTransfer(GetTransferRequest request, StreamObserver<GetTransferResponse> responseObserver) {
        try {
            var res = getTransferUseCase.get(new GetTransferQuery(UUID.fromString(request.getTransferId())));
            responseObserver.onNext(GetTransferResponse.newBuilder().setTransfer(toProto(res.transfer())).build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listMyTransfers(ListMyTransfersRequest request, StreamObserver<ListMyTransfersResponse> responseObserver) {
        try {
            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();

            TransferStatus status = toDomainStatus(request.getStatus());
            UUID fromAccountId = request.getFromAccountId() == null || request.getFromAccountId().isBlank()
                    ? null
                    : UUID.fromString(request.getFromAccountId());
            UUID toAccountId = request.getToAccountId() == null || request.getToAccountId().isBlank()
                    ? null
                    : UUID.fromString(request.getToAccountId());

            var res = listMyTransfersUseCase.list(new ListMyTransfersQuery(
                    userId,
                    status,
                    fromAccountId,
                    toAccountId,
                    request.getLimit(),
                    request.getOffset()
            ));

            var b = ListMyTransfersResponse.newBuilder();
            for (Transfer t : res.transfers()) {
                b.addTransfers(toProto(t));
            }

            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void adminMint(AdminMintRequest request, StreamObserver<AdminMintResponse> responseObserver) {
        try {
            requireAdmin();

            UUID userId = AuthMetadataServerInterceptor.USER_ID_CTX_KEY.get();
            var res = adminMintUseCase.mint(new AdminMintCommand(
                    userId,
                    UUID.fromString(request.getToAccountId()),
                    request.getAmountCents(),
                    request.getIdempotencyKey(),
                    request.getDescription()
            ));

            var b = AdminMintResponse.newBuilder()
                    .setTransfer(toProto(res.transfer()))
                    .setTreasuryBalanceCents(res.treasuryBalanceCents())
                    .setToBalanceCents(res.toBalanceCents());

            if (res.ledgerEntry() != null) {
                b.setEntry(res.ledgerEntry());
            }

            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    private void requireAdmin() {
        String role = AuthMetadataServerInterceptor.ROLE_CTX_KEY.get();
        if (!"ADMIN".equals(role)) {
            throw Status.PERMISSION_DENIED.withDescription("ADMIN role required").asRuntimeException();
        }
    }

    private static com.banking.transfers.v1.Transfer toProto(Transfer t) {
        return com.banking.transfers.v1.Transfer.newBuilder()
                .setId(t.id().toString())
                .setInitiatorUserId(t.initiatorUserId().toString())
                .setFromAccountId(t.fromAccountId().toString())
                .setToAccountId(t.toAccountId().toString())
                .setAmountCents(t.amountCents())
                .setIdempotencyKey(t.idempotencyKey())
                .setDescription(t.description() == null ? "" : t.description())
                .setCreatedAtEpochMs(t.createdAtEpochMs())
                .setStatus(toProtoStatus(t.status()))
                .setLedgerEntryId(t.ledgerEntryId() == null ? "" : t.ledgerEntryId().toString())
                .setFailureMessage(t.failureMessage() == null ? "" : t.failureMessage())
                .setFeeCents(t.feeCents())
                .setFeeLedgerEntryId(t.feeLedgerEntryId() == null ? "" : t.feeLedgerEntryId().toString())
                .build();
    }

    private static com.banking.transfers.v1.TransferStatus toProtoStatus(TransferStatus s) {
        if (s == null) {
            return com.banking.transfers.v1.TransferStatus.TRANSFER_STATUS_UNSPECIFIED;
        }
        return switch (s) {
            case PENDING -> com.banking.transfers.v1.TransferStatus.TRANSFER_STATUS_PENDING;
            case COMPLETED -> com.banking.transfers.v1.TransferStatus.TRANSFER_STATUS_COMPLETED;
            case BLOCKED -> com.banking.transfers.v1.TransferStatus.TRANSFER_STATUS_BLOCKED;
            case FAILED -> com.banking.transfers.v1.TransferStatus.TRANSFER_STATUS_FAILED;
        };
    }

    private static TransferStatus toDomainStatus(com.banking.transfers.v1.TransferStatus status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case TRANSFER_STATUS_PENDING -> TransferStatus.PENDING;
            case TRANSFER_STATUS_COMPLETED -> TransferStatus.COMPLETED;
            case TRANSFER_STATUS_BLOCKED -> TransferStatus.BLOCKED;
            case TRANSFER_STATUS_FAILED -> TransferStatus.FAILED;
            case TRANSFER_STATUS_UNSPECIFIED, UNRECOGNIZED -> null;
        };
    }
}
