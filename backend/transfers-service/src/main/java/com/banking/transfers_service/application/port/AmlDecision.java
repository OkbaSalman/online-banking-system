package com.banking.transfers_service.application.port;

public record AmlDecision(
        boolean allowed,
        String reason
) {
    public static AmlDecision allow() {
        return new AmlDecision(true, "");
    }

    public static AmlDecision block(String reason) {
        return new AmlDecision(false, reason == null ? "Blocked" : reason);
    }
}
