package com.banking.ledger_service.adapter.in.grpc;

import com.banking.ledger.v1.*;
import com.banking.ledger_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import com.banking.ledger_service.application.usecase.create_transfer.CreateTransferUseCase;
import com.banking.ledger_service.application.usecase.create_transfer.dto.CreateTransferCommand;
import com.banking.ledger_service.application.usecase.get_balance.GetBalanceUseCase;
import com.banking.ledger_service.application.usecase.get_balance.dto.GetBalanceQuery;
import com.banking.ledger_service.application.usecase.get_entry.GetEntryUseCase;
import com.banking.ledger_service.application.usecase.get_entry.dto.GetEntryQuery;
import com.banking.ledger_service.application.usecase.list_entries.ListEntriesUseCase;
import com.banking.ledger_service.application.usecase.list_entries.dto.ListEntriesQuery;
import com.banking.ledger_service.application.usecase.get_my_chain_head.GetMyChainHeadUseCase;
import com.banking.ledger_service.application.usecase.get_my_chain_head.dto.GetMyChainHeadQuery;
import com.banking.ledger_service.application.usecase.verify_my_chain.VerifyMyChainUseCase;
import com.banking.ledger_service.application.usecase.verify_my_chain.dto.VerifyMyChainQuery;
import com.banking.ledger_service.domain.model.AccountLedgerItem;
import com.banking.ledger_service.domain.model.LedgerEntry;
import com.banking.ledger_service.domain.model.Posting;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LedgerGrpcController extends LedgerServiceGrpc.LedgerServiceImplBase {

    private final CreateTransferUseCase createTransferUseCase;
    private final GetBalanceUseCase getBalanceUseCase;
    private final GetEntryUseCase getEntryUseCase;
    private final ListEntriesUseCase listEntriesUseCase;
    private final GetMyChainHeadUseCase getMyChainHeadUseCase;
    private final VerifyMyChainUseCase verifyMyChainUseCase;

    public LedgerGrpcController(
            CreateTransferUseCase createTransferUseCase,
            GetBalanceUseCase getBalanceUseCase,
            GetEntryUseCase getEntryUseCase,
            ListEntriesUseCase listEntriesUseCase,
            GetMyChainHeadUseCase getMyChainHeadUseCase,
            VerifyMyChainUseCase verifyMyChainUseCase
    ) {
        this.createTransferUseCase = createTransferUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
        this.getEntryUseCase = getEntryUseCase;
        this.listEntriesUseCase = listEntriesUseCase;
        this.getMyChainHeadUseCase = getMyChainHeadUseCase;
        this.verifyMyChainUseCase = verifyMyChainUseCase;
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
            String role = AuthMetadataServerInterceptor.ROLE_CTX_KEY.get();

            if (!"ADMIN".equalsIgnoreCase(role)) {
                responseObserver.onError(Status.PERMISSION_DENIED
                        .withDescription("Admin role required")
                        .asRuntimeException());
                return;
            }

            var cmd = new CreateTransferCommand(
                    userId,
                    UUID.fromString(request.getFromAccountId()),
                    UUID.fromString(request.getToAccountId()),
                    request.getAmountCents(),
                    request.getIdempotencyKey(),
                    request.getDescription()
            );

            var result = createTransferUseCase.create(cmd);

            responseObserver.onNext(CreateTransferResponse.newBuilder()
                    .setEntry(toProto(result.entry()))
                    .setFromBalanceCents(result.fromBalanceCents())
                    .setToBalanceCents(result.toBalanceCents())
                    .build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void getBalance(GetBalanceRequest request, StreamObserver<GetBalanceResponse> responseObserver) {
        try {
            UUID accountId = UUID.fromString(request.getAccountId());

            var result = getBalanceUseCase.get(new GetBalanceQuery(accountId));

            responseObserver.onNext(GetBalanceResponse.newBuilder()
                    .setAccountId(result.accountId().toString())
                    .setAvailableCents(result.availableCents())
                    .build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void getEntry(GetEntryRequest request, StreamObserver<GetEntryResponse> responseObserver) {
        try {
            UUID entryId = UUID.fromString(request.getEntryId());

            var result = getEntryUseCase.get(new GetEntryQuery(entryId));

            responseObserver.onNext(GetEntryResponse.newBuilder()
                    .setEntry(toProto(result.entry()))
                    .build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void listAccountEntries(ListAccountEntriesRequest request, StreamObserver<ListAccountEntriesResponse> responseObserver) {
        try {
            UUID accountId = UUID.fromString(request.getAccountId());

            var result = listEntriesUseCase.list(new ListEntriesQuery(accountId, request.getLimit(), request.getOffset()));

            var b = ListAccountEntriesResponse.newBuilder();
            for (AccountLedgerItem item : result.items()) {
                b.addItems(toProto(item));
            }

            responseObserver.onNext(b.build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void getAccountChainHead(GetAccountChainHeadRequest request, StreamObserver<GetAccountChainHeadResponse> responseObserver) {
        try {
            UUID accountId = UUID.fromString(request.getAccountId());

            var result = getMyChainHeadUseCase.get(new GetMyChainHeadQuery(accountId));

            responseObserver.onNext(GetAccountChainHeadResponse.newBuilder()
                    .setAccountId(accountId.toString())
                    .setHeadSeq(result.headSeq())
                    .setHeadHash(result.headHash() == null ? "" : result.headHash())
                    .setHeadEntryId(result.headEntryId() == null ? "" : result.headEntryId().toString())
                    .build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    @Override
    public void verifyAccountChain(VerifyAccountChainRequest request, StreamObserver<VerifyAccountChainResponse> responseObserver) {
        try {
            UUID accountId = UUID.fromString(request.getAccountId());

            var result = verifyMyChainUseCase.verify(new VerifyMyChainQuery(accountId));

            responseObserver.onNext(VerifyAccountChainResponse.newBuilder()
                    .setOk(result.ok())
                    .setFirstInvalidSeq(result.firstInvalidSeq())
                    .setMessage(result.message())
                    .build());
            responseObserver.onCompleted();
        } catch (Throwable t) {
            responseObserver.onError(GrpcErrorMapper.toStatus(t).asRuntimeException());
        }
    }

    private com.banking.ledger.v1.LedgerEntry toProto(LedgerEntry entry) {
        var b = com.banking.ledger.v1.LedgerEntry.newBuilder()
                .setId(entry.id().toString())
                .setInitiatorUserId(entry.initiatorUserId().toString())
                .setIdempotencyKey(entry.idempotencyKey())
                .setType(entry.type())
                .setDescription(entry.description() == null ? "" : entry.description())
                .setCreatedAtEpochMs(entry.createdAtEpochMs())
                .setFromAccountId(entry.fromAccountId().toString())
                .setToAccountId(entry.toAccountId().toString())
                .setAmountCents(entry.amountCents());

        for (Posting p : entry.postings()) {
            b.addPostings(com.banking.ledger.v1.Posting.newBuilder()
                    .setAccountId(p.accountId().toString())
                    .setAmountCents(p.amountCents())
                    .build());
        }

        return b.build();
    }

    private com.banking.ledger.v1.AccountLedgerItem toProto(AccountLedgerItem item) {
        return com.banking.ledger.v1.AccountLedgerItem.newBuilder()
                .setId(item.id().toString())
                .setAccountId(item.accountId().toString())
                .setEntryId(item.entryId().toString())
                .setCreatedAtEpochMs(item.createdAtEpochMs())
                .setAmountCents(item.amountCents())
                .setCounterpartyAccountId(item.counterpartyAccountId().toString())
                .setSeq(item.seq())
                .setPrevHash(item.prevHash() == null ? "" : item.prevHash())
                .setItemHash(item.itemHash())
                .setEntry(toProto(item.entry()))
                .build();
    }
}