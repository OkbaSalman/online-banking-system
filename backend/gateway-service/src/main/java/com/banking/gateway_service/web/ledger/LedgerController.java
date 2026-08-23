package com.banking.gateway_service.web.ledger;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.gateway_service.grpc.security.GrpcAuthContext;
import com.banking.gateway_service.web.ledger.dto.balance.GetBalanceHttpResponse;
import com.banking.gateway_service.web.ledger.dto.chain.GetChainHeadHttpResponse;
import com.banking.gateway_service.web.ledger.dto.chain.VerifyChainHttpResponse;
import com.banking.gateway_service.web.ledger.dto.common.AccountLedgerItemHttpDto;
import com.banking.gateway_service.web.ledger.dto.common.LedgerEntryHttpDto;
import com.banking.gateway_service.web.ledger.dto.common.PostingHttpDto;
import com.banking.gateway_service.web.ledger.dto.entry.GetEntryHttpResponse;
import com.banking.gateway_service.web.ledger.dto.list.ListAccountEntriesHttpResponse;
import com.banking.gateway_service.web.ledger.dto.transfer.CreateLedgerTransferHttpRequest;
import com.banking.gateway_service.web.ledger.dto.transfer.CreateLedgerTransferHttpResponse;
import com.banking.ledger.v1.AccountLedgerItem;
import com.banking.ledger.v1.CreateTransferRequest;
import com.banking.ledger.v1.GetAccountChainHeadRequest;
import com.banking.ledger.v1.GetBalanceRequest;
import com.banking.ledger.v1.GetEntryRequest;
import com.banking.ledger.v1.LedgerEntry;
import com.banking.ledger.v1.ListAccountEntriesRequest;
import com.banking.ledger.v1.PingRequest;
import com.banking.ledger.v1.Posting;
import com.banking.ledger.v1.VerifyAccountChainRequest;
import com.banking.ledger.v1.LedgerServiceGrpc;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class LedgerController {

    private final LedgerServiceGrpc.LedgerServiceBlockingStub ledger;

    public record PingHttpResponse(String message) {}

    public LedgerController(LedgerServiceGrpc.LedgerServiceBlockingStub ledger) {
        this.ledger = ledger;
    }

    @GetMapping("/api/ledger/ping")
    public Mono<PingHttpResponse> ping(Authentication authentication, @RequestParam(defaultValue = "hello") String message) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> ledger.ping(PingRequest.newBuilder().setMessage(message).build())))
                .subscribeOn(Schedulers.boundedElastic())
                .map(res -> new PingHttpResponse(res.getMessage()));
    }

    @PostMapping("/api/ledger/transfers")
    public Mono<CreateLedgerTransferHttpResponse> createTransfer(Authentication authentication, @RequestBody CreateLedgerTransferHttpRequest body) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> ledger.createTransfer(
                CreateTransferRequest.newBuilder()
                        .setFromAccountId(body.fromAccountId())
                        .setToAccountId(body.toAccountId())
                        .setAmountCents(body.amountCents())
                        .setIdempotencyKey(body.idempotencyKey())
                        .setDescription(body.description())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new CreateLedgerTransferHttpResponse(
                  toHttp(res.getEntry()),
                  res.getFromBalanceCents(),
                  res.getToBalanceCents()
          ));
    }

    @GetMapping("/api/ledger/balance/{accountId}")
    public Mono<GetBalanceHttpResponse> getBalance(Authentication authentication, @PathVariable String accountId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> ledger.getBalance(
                GetBalanceRequest.newBuilder().setAccountId(accountId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new GetBalanceHttpResponse(res.getAccountId(), res.getAvailableCents()));
    }

    @GetMapping("/api/ledger/entries/{entryId}")
    public Mono<GetEntryHttpResponse> getEntry(Authentication authentication, @PathVariable String entryId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> ledger.getEntry(
                GetEntryRequest.newBuilder().setEntryId(entryId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new GetEntryHttpResponse(toHttp(res.getEntry())));
    }

    @GetMapping("/api/ledger/accounts/{accountId}/entries")
    public Mono<ListAccountEntriesHttpResponse> listAccountEntries(
            Authentication authentication,
            @PathVariable String accountId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> ledger.listAccountEntries(
                ListAccountEntriesRequest.newBuilder()
                        .setAccountId(accountId)
                        .setLimit(limit)
                        .setOffset(offset)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListAccountEntriesHttpResponse(res.getItemsList().stream().map(LedgerController::toHttp).toList()));
    }

    @GetMapping("/api/ledger/accounts/{accountId}/chain-head")
    public Mono<GetChainHeadHttpResponse> getChainHead(Authentication authentication, @PathVariable String accountId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> ledger.getAccountChainHead(
                GetAccountChainHeadRequest.newBuilder().setAccountId(accountId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new GetChainHeadHttpResponse(res.getAccountId(), res.getHeadSeq(), res.getHeadHash(), res.getHeadEntryId()));
    }

    @GetMapping("/api/ledger/accounts/{accountId}/verify-chain")
    public Mono<VerifyChainHttpResponse> verifyChain(Authentication authentication, @PathVariable String accountId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> ledger.verifyAccountChain(
                VerifyAccountChainRequest.newBuilder().setAccountId(accountId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new VerifyChainHttpResponse(res.getOk(), res.getFirstInvalidSeq(), res.getMessage()));
    }

    private static LedgerEntryHttpDto toHttp(LedgerEntry e) {
        List<PostingHttpDto> postings = e.getPostingsList().stream().map(LedgerController::toHttp).toList();
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

    private static AccountLedgerItemHttpDto toHttp(AccountLedgerItem item) {
        return new AccountLedgerItemHttpDto(
                item.getId(),
                item.getAccountId(),
                item.getEntryId(),
                item.getCreatedAtEpochMs(),
                item.getAmountCents(),
                item.getCounterpartyAccountId(),
                item.getSeq(),
                item.getPrevHash(),
                item.getItemHash(),
                toHttp(item.getEntry())
        );
    }
}