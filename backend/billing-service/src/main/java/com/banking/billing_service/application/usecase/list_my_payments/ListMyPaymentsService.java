package com.banking.billing_service.application.usecase.list_my_payments;

import com.banking.billing_service.application.port.BillingPaymentQueryPort;
import com.banking.billing_service.application.usecase.list_my_payments.dto.ListMyPaymentsQuery;
import com.banking.billing_service.application.usecase.list_my_payments.dto.ListMyPaymentsResult;

public class ListMyPaymentsService implements ListMyPaymentsUseCase {

    private final BillingPaymentQueryPort payments;

    public ListMyPaymentsService(BillingPaymentQueryPort payments) {
        this.payments = payments;
    }

    @Override
    public ListMyPaymentsResult list(ListMyPaymentsQuery query) {
        if (query.userId() == null) {
            throw new IllegalArgumentException("user_id is required");
        }
        return new ListMyPaymentsResult(payments.listByUserId(query.userId(), query.limit(), query.offset()));
    }
}
