package com.banking.billing_service.application.usecase.list_my_payments;

import com.banking.billing_service.application.usecase.list_my_payments.dto.ListMyPaymentsQuery;
import com.banking.billing_service.application.usecase.list_my_payments.dto.ListMyPaymentsResult;

public interface ListMyPaymentsUseCase {
    ListMyPaymentsResult list(ListMyPaymentsQuery query);
}
