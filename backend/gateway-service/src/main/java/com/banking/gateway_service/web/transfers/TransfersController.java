package com.banking.gateway_service.web.transfers;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.gateway_service.grpc.security.GrpcAuthContext;
import com.banking.gateway_service.web.ledger.dto.common.LedgerEntryHttpDto;
import com.banking.gateway_service.web.ledger.dto.common.PostingHttpDto;
import com.banking.gateway_service.web.transfers.dto.create.CreateTransferHttpRequest;
import com.banking.gateway_service.web.transfers.dto.create.CreateTransferHttpResponse;
import com.banking.gateway_service.web.transfers.dto.get.GetTransferHttpResponse;
import com.banking.gateway_service.web.transfers.dto.mint.AdminMintHttpRequest;
import com.banking.gateway_service.web.transfers.dto.mint.AdminMintHttpResponse;
import com.banking.gateway_service.web.transfers.dto.transfer.TransferHttpDto;
import com.banking.ledger.v1.LedgerEntry;
import com.banking.ledger.v1.Posting;
import com.banking.transfers.v1.PingRequest;
import com.banking.transfers.v1.AdminGetRevenueSummaryRequest;
import com.banking.transfers.v1.AdminListTransfersRequest;
import com.banking.transfers.v1.AdminMintRequest;
import com.banking.transfers.v1.CreateTransferRequest;
import com.banking.transfers.v1.GetTransferRequest;
import com.banking.transfers.v1.ListMyTransfersRequest;
import com.banking.transfers.v1.Transfer;
import com.banking.transfers.v1.TransferStatus;
import com.banking.transfers.v1.TransfersServiceGrpc;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class TransfersController {

    private final TransfersServiceGrpc.TransfersServiceBlockingStub transfers;

    public record PingHttpResponse(String message) {}

    public record ListTransfersHttpResponse(List<TransferHttpDto> transfers) {}

    public record MonthlyRevenueHttpDto(
            int year,
            int month,
            long feeCents,
            long volumeCents,
            int transferCount
    ) {}

    public record AdminRevenueSummaryHttpResponse(
            int year,
            Integer month,
            long feeCents,
            long volumeCents,
            int transferCount,
            List<MonthlyRevenueHttpDto> months
    ) {}

    public TransfersController(TransfersServiceGrpc.TransfersServiceBlockingStub transfers) {
        this.transfers = transfers;
    }

    @GetMapping("/api/transfers/ping")
    public Mono<PingHttpResponse> ping(Authentication authentication, @RequestParam(defaultValue = "hello") String message) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> transfers.ping(PingRequest.newBuilder().setMessage(message).build())))
                .subscribeOn(Schedulers.boundedElastic())
                .map(res -> new PingHttpResponse(res.getMessage()));
    }

    @PostMapping("/api/transfers")
    public Mono<CreateTransferHttpResponse> createTransfer(Authentication authentication, @RequestBody CreateTransferHttpRequest body) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> transfers.createTransfer(
                CreateTransferRequest.newBuilder()
                        .setFromAccountId(body.fromAccountId())
                        .setToAccountId(body.toAccountId())
                        .setAmountCents(body.amountCents())
                        .setIdempotencyKey(body.idempotencyKey())
                        .setDescription(body.description())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new CreateTransferHttpResponse(
                  toHttp(res.getTransfer()),
                  toHttp(res.getEntry()),
                  res.getFromBalanceCents(),
                  res.getToBalanceCents()
          ));
    }

    @GetMapping("/api/transfers/{transferId}")
    public Mono<GetTransferHttpResponse> getTransfer(Authentication authentication, @PathVariable String transferId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> transfers.getTransfer(
                GetTransferRequest.newBuilder().setTransferId(transferId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new GetTransferHttpResponse(toHttp(res.getTransfer())));
    }

    @GetMapping("/api/transfers")
    public Mono<ListTransfersHttpResponse> listMyTransfers(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromAccountId,
            @RequestParam(required = false) String toAccountId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> transfers.listMyTransfers(
                ListMyTransfersRequest.newBuilder()
                        .setStatus(parseTransferStatus(status))
                        .setFromAccountId(fromAccountId == null ? "" : fromAccountId)
                        .setToAccountId(toAccountId == null ? "" : toAccountId)
                        .setLimit(limit)
                        .setOffset(offset)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListTransfersHttpResponse(res.getTransfersList().stream().map(TransfersController::toHttp).toList()));
    }

    @PostMapping("/api/transfers/admin/mint")
    public Mono<AdminMintHttpResponse> adminMint(Authentication authentication, @RequestBody AdminMintHttpRequest body) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> transfers.adminMint(
                AdminMintRequest.newBuilder()
                        .setToAccountId(body.toAccountId())
                        .setAmountCents(body.amountCents())
                        .setIdempotencyKey(body.idempotencyKey())
                        .setDescription(body.description())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new AdminMintHttpResponse(
                  toHttp(res.getTransfer()),
                  toHttp(res.getEntry()),
                  res.getTreasuryBalanceCents(),
                  res.getToBalanceCents()
          ));
    }

    @GetMapping("/api/transfers/admin")
    public Mono<ListTransfersHttpResponse> adminListTransfers(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String initiatorUserId,
            @RequestParam(required = false) String fromAccountId,
            @RequestParam(required = false) String toAccountId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> transfers.adminListTransfers(
                AdminListTransfersRequest.newBuilder()
                        .setStatus(parseTransferStatus(status))
                        .setInitiatorUserId(initiatorUserId == null ? "" : initiatorUserId)
                        .setFromAccountId(fromAccountId == null ? "" : fromAccountId)
                        .setToAccountId(toAccountId == null ? "" : toAccountId)
                        .setLimit(limit)
                        .setOffset(offset)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListTransfersHttpResponse(res.getTransfersList().stream().map(TransfersController::toHttp).toList()));
    }

    @GetMapping("/api/transfers/admin/revenue")
    public Mono<AdminRevenueSummaryHttpResponse> adminGetRevenueSummary(
            Authentication authentication,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> transfers.adminGetRevenueSummary(
                AdminGetRevenueSummaryRequest.newBuilder()
                        .setYear(year == null ? 0 : year)
                        .setMonth(month == null ? 0 : month)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new AdminRevenueSummaryHttpResponse(
                  res.getYear(),
                  res.getMonth() == 0 ? null : res.getMonth(),
                  res.getFeeCents(),
                  res.getVolumeCents(),
                  res.getTransferCount(),
                  res.getMonthsList().stream()
                          .map(bucket -> new MonthlyRevenueHttpDto(
                                  bucket.getYear(),
                                  bucket.getMonth(),
                                  bucket.getFeeCents(),
                                  bucket.getVolumeCents(),
                                  bucket.getTransferCount()
                          ))
                          .toList()
          ));
    }

    private static TransferHttpDto toHttp(Transfer t) {
        return new TransferHttpDto(
                t.getId(),
                t.getInitiatorUserId(),
                t.getFromAccountId(),
                t.getToAccountId(),
                t.getAmountCents(),
                t.getIdempotencyKey(),
                t.getDescription(),
                t.getCreatedAtEpochMs(),
                t.getStatus().name(),
                t.getLedgerEntryId(),
                t.getFailureMessage(),
                t.getFeeCents(),
                t.getFeeLedgerEntryId()
        );
    }

    private static LedgerEntryHttpDto toHttp(LedgerEntry e) {
        List<PostingHttpDto> postings = e.getPostingsList().stream().map(TransfersController::toHttp).toList();
        return new LedgerEntryHttpDto(
                e.getId(),
                e.getInitiatorUserId(),
                e.getIdempotencyKey(),
                e.getType(),
                e.getDescription(),
                e.getCreatedAtEpochMs(),
                e.getFromAccountId(),
                e.getToAccountId(),
                e.getAmountCents(),
                postings
        );
    }

    private static PostingHttpDto toHttp(Posting p) {
        return new PostingHttpDto(p.getAccountId(), p.getAmountCents());
    }

    private static TransferStatus parseTransferStatus(String status) {
        if (status == null) {
            return TransferStatus.TRANSFER_STATUS_UNSPECIFIED;
        }
        String v = status.trim().toUpperCase();
        return switch (v) {
            case "PENDING", "TRANSFER_STATUS_PENDING" -> TransferStatus.TRANSFER_STATUS_PENDING;
            case "COMPLETED", "TRANSFER_STATUS_COMPLETED" -> TransferStatus.TRANSFER_STATUS_COMPLETED;
            case "BLOCKED", "TRANSFER_STATUS_BLOCKED" -> TransferStatus.TRANSFER_STATUS_BLOCKED;
            case "FAILED", "TRANSFER_STATUS_FAILED" -> TransferStatus.TRANSFER_STATUS_FAILED;
            default -> TransferStatus.TRANSFER_STATUS_UNSPECIFIED;
        };
    }
}