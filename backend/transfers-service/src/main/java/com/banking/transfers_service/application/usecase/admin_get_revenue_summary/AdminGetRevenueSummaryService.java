package com.banking.transfers_service.application.usecase.admin_get_revenue_summary;

import com.banking.transfers_service.application.port.TransferQueryPort;
import com.banking.transfers_service.application.usecase.admin_get_revenue_summary.dto.AdminGetRevenueSummaryQuery;
import com.banking.transfers_service.application.usecase.admin_get_revenue_summary.dto.AdminGetRevenueSummaryResult;
import com.banking.transfers_service.domain.model.MonthlyRevenue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class AdminGetRevenueSummaryService implements AdminGetRevenueSummaryUseCase {

    private final TransferQueryPort transfers;

    public AdminGetRevenueSummaryService(TransferQueryPort transfers) {
        this.transfers = transfers;
    }

    @Override
    public AdminGetRevenueSummaryResult summarize(AdminGetRevenueSummaryQuery query) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        int year = query.year() > 0 ? query.year() : now.getYear();
        Integer month = query.month() >= 1 && query.month() <= 12 ? query.month() : null;

        LocalDate fromDate = month == null
                ? LocalDate.of(year, 1, 1)
                : LocalDate.of(year, month, 1);
        LocalDate toDate = month == null ? fromDate.plusYears(1) : fromDate.plusMonths(1);

        long fromMs = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        long toMs = toDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        List<MonthlyRevenue> months = transfers.aggregateCompletedRevenue(fromMs, toMs);
        long feeCents = months.stream().mapToLong(MonthlyRevenue::feeCents).sum();
        long volumeCents = months.stream().mapToLong(MonthlyRevenue::volumeCents).sum();
        int transferCount = months.stream().mapToInt(MonthlyRevenue::transferCount).sum();

        return new AdminGetRevenueSummaryResult(year, month, feeCents, volumeCents, transferCount, months);
    }
}
