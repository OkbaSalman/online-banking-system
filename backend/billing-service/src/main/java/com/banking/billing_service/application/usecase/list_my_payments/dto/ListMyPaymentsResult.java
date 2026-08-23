package com.banking.billing_service.application.usecase.list_my_payments.dto;

import com.banking.billing_service.domain.model.BillingPayment;

import java.util.List;

public record ListMyPaymentsResult(
        List<BillingPayment> payments
) {}
