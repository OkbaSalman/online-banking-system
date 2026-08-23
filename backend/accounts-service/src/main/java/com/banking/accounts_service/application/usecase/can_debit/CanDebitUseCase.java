package com.banking.accounts_service.application.usecase.can_debit;

import com.banking.accounts_service.application.usecase.can_debit.dto.CanDebitQuery;
import com.banking.accounts_service.application.usecase.can_debit.dto.CanDebitResult;

public interface CanDebitUseCase {
    CanDebitResult canDebit(CanDebitQuery query);
}
