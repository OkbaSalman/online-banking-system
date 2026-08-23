package com.banking.transfers_service.application.usecase.admin_get_revenue_summary;

import com.banking.transfers_service.application.usecase.admin_get_revenue_summary.dto.AdminGetRevenueSummaryQuery;
import com.banking.transfers_service.application.usecase.admin_get_revenue_summary.dto.AdminGetRevenueSummaryResult;

public interface AdminGetRevenueSummaryUseCase {
    AdminGetRevenueSummaryResult summarize(AdminGetRevenueSummaryQuery query);
}
