package com.banking.ledger_service.adapter.out.jpa;

import com.banking.ledger_service.adapter.out.jpa.entity.*;
import com.banking.ledger_service.adapter.out.jpa.repository.*;
import com.banking.ledger_service.application.port.LedgerRepositoryPort;
import com.banking.ledger_service.application.port.LedgerWriteResult;
import com.banking.ledger_service.application.port.VerifyChainScanResult;
import com.banking.ledger_service.application.usecase.common.LedgerHashing;
import com.banking.ledger_service.application.usecase.common.exception.InsufficientFundsException;
import com.banking.ledger_service.application.usecase.common.exception.NotFoundException;
import com.banking.ledger_service.domain.model.AccountLedgerItem;
import com.banking.ledger_service.domain.model.Balance;
import com.banking.ledger_service.domain.model.ChainHead;
import com.banking.ledger_service.domain.model.LedgerEntry;
import com.banking.ledger_service.domain.model.Posting;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class LedgerJpaAdapter implements LedgerRepositoryPort {

    private final AccountBalanceJpaRepository balances;
    private final LedgerChainHeadJpaRepository heads;
    private final LedgerEntryJpaRepository entries;
    private final PostingJpaRepository postings;
    private final AccountLedgerItemJpaRepository accountItems;

    public LedgerJpaAdapter(
            AccountBalanceJpaRepository balances,
            LedgerChainHeadJpaRepository heads,
            LedgerEntryJpaRepository entries,
            PostingJpaRepository postings,
            AccountLedgerItemJpaRepository accountItems
    ) {
        this.balances = balances;
        this.heads = heads;
        this.entries = entries;
        this.postings = postings;
        this.accountItems = accountItems;
    }

    @Override
    public Optional<LedgerEntry> findByIdempotencyKey(UUID initiatorUserId, String idempotencyKey) {
        return entries.findByInitiatorUserIdAndIdempotencyKey(initiatorUserId, idempotencyKey)
                .map(this::toDomain);
    }

    @Transactional
    @Override
    public LedgerWriteResult createTransfer(
            UUID initiatorUserId,
            UUID fromAccountId,
            UUID toAccountId,
            long amountCents,
            String idempotencyKey,
            String description
    ) {
        entries.findByInitiatorUserIdAndIdempotencyKey(initiatorUserId, idempotencyKey).ifPresent(existing -> {
            throw new IllegalArgumentException("Duplicate idempotency_key");
        });

        ensureBalanceRowExists(fromAccountId);
        ensureBalanceRowExists(toAccountId);

        List<UUID> lockIds = new ArrayList<>(List.of(fromAccountId, toAccountId));
        lockIds.sort(UUID::compareTo);

        List<AccountBalanceEntity> locked = balances.findAllForUpdate(lockIds);
        Map<UUID, AccountBalanceEntity> byId = new HashMap<>();
        for (AccountBalanceEntity b : locked) {
            byId.put(b.getAccountId(), b);
        }

        AccountBalanceEntity fromBal = byId.get(fromAccountId);
        AccountBalanceEntity toBal = byId.get(toAccountId);

        if (fromBal == null || toBal == null) {
            throw new IllegalStateException("Balance rows missing");
        }

        if (fromBal.getAvailableCents() < amountCents) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        fromBal.setAvailableCents(fromBal.getAvailableCents() - amountCents);
        toBal.setAvailableCents(toBal.getAvailableCents() + amountCents);

        UUID entryId = UUID.randomUUID();
        long now = System.currentTimeMillis();

        LedgerChainHeadEntity fromHead = getOrCreateHeadForUpdate(fromAccountId);
        LedgerChainHeadEntity toHead = getOrCreateHeadForUpdate(toAccountId);

        UUID fromItemId = UUID.randomUUID();
        long fromSeq = fromHead.getHeadSeq() + 1;
        String fromPrevHash = fromHead.getHeadHash() == null ? "" : fromHead.getHeadHash();
        String fromItemHash = LedgerHashing.hashAccountLedgerItem(
                fromAccountId,
                fromSeq,
                fromItemId,
                entryId,
                fromPrevHash,
                now,
                -amountCents,
                toAccountId,
                idempotencyKey,
                fromAccountId,
                toAccountId,
                amountCents,
                description
        );

        UUID toItemId = UUID.randomUUID();
        long toSeq = toHead.getHeadSeq() + 1;
        String toPrevHash = toHead.getHeadHash() == null ? "" : toHead.getHeadHash();
        String toItemHash = LedgerHashing.hashAccountLedgerItem(
                toAccountId,
                toSeq,
                toItemId,
                entryId,
                toPrevHash,
                now,
                amountCents,
                fromAccountId,
                idempotencyKey,
                fromAccountId,
                toAccountId,
                amountCents,
                description
        );

        LedgerEntryEntity entryEntity = new LedgerEntryEntity(
                entryId,
                initiatorUserId,
                idempotencyKey,
                "TRANSFER",
                description,
                now,
                fromAccountId,
                toAccountId,
                amountCents
        );

        PostingEntity debit = new PostingEntity(UUID.randomUUID(), entryId, fromAccountId, -amountCents);
        PostingEntity credit = new PostingEntity(UUID.randomUUID(), entryId, toAccountId, amountCents);

        try {
            entries.save(entryEntity);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Duplicate idempotency_key");
        }

        postings.save(debit);
        postings.save(credit);

        accountItems.save(new AccountLedgerItemEntity(
                fromItemId,
                fromAccountId,
                entryId,
                now,
                -amountCents,
                toAccountId,
                fromSeq,
                fromPrevHash,
                fromItemHash
        ));

        accountItems.save(new AccountLedgerItemEntity(
                toItemId,
                toAccountId,
                entryId,
                now,
                amountCents,
                fromAccountId,
                toSeq,
                toPrevHash,
                toItemHash
        ));

        balances.save(fromBal);
        balances.save(toBal);

        fromHead.setHeadSeq(fromSeq);
        fromHead.setHeadHash(fromItemHash);
        fromHead.setHeadEntryId(entryId);
        heads.save(fromHead);

        toHead.setHeadSeq(toSeq);
        toHead.setHeadHash(toItemHash);
        toHead.setHeadEntryId(entryId);
        heads.save(toHead);

        LedgerEntry domain = new LedgerEntry(
                entryId,
                initiatorUserId,
                idempotencyKey,
                "TRANSFER",
                description,
                now,
                fromAccountId,
                toAccountId,
                amountCents,
                List.of(
                        new Posting(fromAccountId, -amountCents),
                        new Posting(toAccountId, amountCents)
                )
        );

        return new LedgerWriteResult(domain, fromBal.getAvailableCents(), toBal.getAvailableCents());
    }

    @Override
    public Balance getBalance(UUID accountId) {
        long cents = balances.findByAccountId(accountId)
                .map(AccountBalanceEntity::getAvailableCents)
                .orElse(0L);
        return new Balance(accountId, cents);
    }

    @Override
    public LedgerEntry getEntry(UUID entryId) {
        LedgerEntryEntity e = entries.findById(entryId)
                .orElseThrow(() -> new NotFoundException("Entry not found"));
        return toDomain(e);
    }

    @Override
    public List<AccountLedgerItem> listAccountEntries(UUID accountId, int limit, int offset) {
        int page = offset / limit;
        int offsetInPage = offset % limit;

        List<AccountLedgerItemEntity> pageItems = accountItems.findByAccountIdOrderByCreatedAtEpochMsDesc(
                accountId,
                PageRequest.of(page, limit + offsetInPage)
        );

        List<AccountLedgerItem> result = new ArrayList<>();
        for (int i = offsetInPage; i < pageItems.size() && result.size() < limit; i++) {
            result.add(toDomain(pageItems.get(i)));
        }
        return result;
    }

    @Override
    public ChainHead getChainHead(UUID accountId) {
        return heads.findById(accountId)
                .map(h -> new ChainHead(accountId, h.getHeadSeq(), h.getHeadHash(), h.getHeadEntryId()))
                .orElse(new ChainHead(accountId, 0L, "", null));
    }

    @Override
    public VerifyChainScanResult verifyChain(UUID accountId) {
        List<AccountLedgerItemEntity> chain = accountItems.findByAccountIdOrderBySeqAsc(accountId);

        String prevHash = "";
        long expectedSeq = 1;

        for (AccountLedgerItemEntity item : chain) {
            if (item.getSeq() != expectedSeq) {
                return new VerifyChainScanResult(false, expectedSeq, "Sequence gap or reordering detected");
            }

            LedgerEntryEntity entry = entries.findById(item.getEntryId())
                    .orElseThrow(() -> new IllegalStateException("Entry missing for item"));

            String expectedHash = LedgerHashing.hashAccountLedgerItem(
                    item.getAccountId(),
                    item.getSeq(),
                    item.getId(),
                    item.getEntryId(),
                    prevHash,
                    item.getCreatedAtEpochMs(),
                    item.getAmountCents(),
                    item.getCounterpartyAccountId(),
                    entry.getIdempotencyKey(),
                    entry.getFromAccountId(),
                    entry.getToAccountId(),
                    entry.getAmountCents(),
                    entry.getDescription()
            );

            if (!Objects.equals(item.getPrevHash(), prevHash)) {
                return new VerifyChainScanResult(false, item.getSeq(), "prev_hash mismatch");
            }
            if (!Objects.equals(item.getItemHash(), expectedHash)) {
                return new VerifyChainScanResult(false, item.getSeq(), "item_hash mismatch");
            }

            prevHash = item.getItemHash();
            expectedSeq++;
        }

        return new VerifyChainScanResult(true, 0L, "OK");
    }

    private LedgerEntry toDomain(LedgerEntryEntity e) {
        List<PostingEntity> ps = postings.findByEntryId(e.getId());
        List<Posting> domainPostings = new ArrayList<>();
        for (PostingEntity p : ps) {
            domainPostings.add(new Posting(p.getAccountId(), p.getAmountCents()));
        }

        return new LedgerEntry(
                e.getId(),
                e.getInitiatorUserId(),
                e.getIdempotencyKey(),
                e.getType(),
                e.getDescription(),
                e.getCreatedAtEpochMs(),
                e.getFromAccountId(),
                e.getToAccountId(),
                e.getAmountCents(),
                domainPostings
        );
    }

    private AccountLedgerItem toDomain(AccountLedgerItemEntity item) {
        LedgerEntryEntity entry = entries.findById(item.getEntryId())
                .orElseThrow(() -> new IllegalStateException("Entry missing for item"));
        return new AccountLedgerItem(
                item.getId(),
                item.getAccountId(),
                item.getEntryId(),
                item.getCreatedAtEpochMs(),
                item.getAmountCents(),
                item.getCounterpartyAccountId(),
                item.getSeq(),
                item.getPrevHash(),
                item.getItemHash(),
                toDomain(entry)
        );
    }

    private void ensureBalanceRowExists(UUID accountId) {
        if (balances.findByAccountId(accountId).isEmpty()) {
            balances.save(new AccountBalanceEntity(accountId, 0L));
        }
    }

    private LedgerChainHeadEntity getOrCreateHeadForUpdate(UUID accountId) {
        Optional<LedgerChainHeadEntity> existing = heads.findForUpdate(accountId);
        if (existing.isPresent()) {
            return existing.get();
        }
        try {
            heads.save(new LedgerChainHeadEntity(accountId, 0L, "", null));
        } catch (DataIntegrityViolationException ignored) {
        }
        return heads.findForUpdate(accountId)
                .orElseThrow(() -> new IllegalStateException("Chain head missing"));
    }
}