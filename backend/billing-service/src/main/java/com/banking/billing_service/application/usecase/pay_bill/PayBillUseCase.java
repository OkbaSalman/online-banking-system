package com.banking.billing_service.application.usecase.pay_bill;

import com.banking.billing_service.application.usecase.pay_bill.dto.PayBillCommand;
import com.banking.billing_service.application.usecase.pay_bill.dto.PayBillResult;

public interface PayBillUseCase {
    PayBillResult pay(PayBillCommand command);
}
