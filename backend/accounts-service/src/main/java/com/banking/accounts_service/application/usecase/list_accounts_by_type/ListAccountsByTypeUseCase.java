package com.banking.accounts_service.application.usecase.list_accounts_by_type;

import com.banking.accounts_service.application.usecase.list_accounts_by_type.dto.ListAccountsByTypeQuery;
import com.banking.accounts_service.application.usecase.list_accounts_by_type.dto.ListAccountsByTypeResult;

public interface ListAccountsByTypeUseCase {
    ListAccountsByTypeResult list(ListAccountsByTypeQuery query);
}
