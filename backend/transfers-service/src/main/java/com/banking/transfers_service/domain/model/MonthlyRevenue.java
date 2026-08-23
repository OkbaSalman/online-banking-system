package com.banking.transfers_service.domain.model;

public record MonthlyRevenue(
        int year,
        int month,
        long feeCents,
        long volumeCents,
        int transferCount
) {}
