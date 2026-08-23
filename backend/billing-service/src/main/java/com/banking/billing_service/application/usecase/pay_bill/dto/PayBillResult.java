package com.banking.billing_service.application.usecase.pay_bill.dto;

import com.banking.billing_service.domain.model.BillingPayment;

public record PayBillResult(
        BillingPayment payment
) {}
