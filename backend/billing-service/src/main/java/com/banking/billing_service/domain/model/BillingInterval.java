package com.banking.billing_service.domain.model;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class BillingInterval {

    private BillingInterval() {}

    public static long add(long baseEpochMs, IntervalUnit unit, int intervalCount) {
        if (unit == null) {
            throw new IllegalArgumentException("interval_unit is required");
        }
        if (intervalCount <= 0) {
            throw new IllegalArgumentException("interval_count must be > 0");
        }

        return switch (unit) {
            case DAY -> baseEpochMs + (intervalCount * 24L * 60L * 60L * 1000L);
            case WEEK -> baseEpochMs + (intervalCount * 7L * 24L * 60L * 60L * 1000L);
            case MONTH -> {
                ZonedDateTime zdt = Instant.ofEpochMilli(baseEpochMs).atZone(ZoneOffset.UTC);
                yield zdt.plusMonths(intervalCount).toInstant().toEpochMilli();
            }
        };
    }
}
