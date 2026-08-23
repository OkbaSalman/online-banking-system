package com.banking.auth_service.application.usecase.search_users;

import com.banking.auth_service.application.usecase.search_users.dto.SearchUsersQuery;
import com.banking.auth_service.application.usecase.search_users.dto.SearchUsersResult;

public interface SearchUsersUseCase {
    SearchUsersResult search(SearchUsersQuery query);
}
