package com.banking.transfers_service.application.usecase.admin_get_revenue_summary.dto;

import com.banking.transfers_service.domain.model.MonthlyRevenue;

import java.util.List;

public record AdminGetRevenueSummaryResult(
        int year,
        Integer month,
        long feeCents,
        long volumeCents,
        int transferCount,
        List<MonthlyRevenue> months
) {}
