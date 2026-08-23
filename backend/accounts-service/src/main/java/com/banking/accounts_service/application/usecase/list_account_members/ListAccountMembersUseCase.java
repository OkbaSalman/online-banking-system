package com.banking.accounts_service.application.usecase.list_account_members;

import com.banking.accounts_service.application.usecase.list_account_members.dto.ListAccountMembersQuery;
import com.banking.accounts_service.application.usecase.list_account_members.dto.ListAccountMembersResult;

public interface ListAccountMembersUseCase {
    ListAccountMembersResult list(ListAccountMembersQuery query);
}
