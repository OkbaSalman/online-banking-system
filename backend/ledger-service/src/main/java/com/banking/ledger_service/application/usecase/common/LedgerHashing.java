package com.banking.ledger_service.application.usecase.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public final class LedgerHashing {

    private LedgerHashing() {}

    public static String hashAccountLedgerItem(
            UUID accountId,
            long seq,
            UUID itemId,
            UUID entryId,
            String prevHash,
            long createdAtEpochMs,
            long signedAmountCents,
            UUID counterpartyAccountId,
            String idempotencyKey,
            UUID fromAccountId,
            UUID toAccountId,
            long transferAmountCents,
            String description
    ) {
        String canonical =
                safe(prevHash) + "|" +
                accountId + "|" +
                seq + "|" +
                itemId + "|" +
                entryId + "|" +
                createdAtEpochMs + "|" +
                signedAmountCents + "|" +
                counterpartyAccountId + "|" +
                safe(idempotencyKey) + "|" +
                fromAccountId + "|" +
                toAccountId + "|" +
                transferAmountCents + "|" +
                safe(description);

        return sha256Hex(canonical);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Hashing failed", e);
        }
    }
}