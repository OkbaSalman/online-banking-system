package com.banking.auth_service.application.usecase.search_users;

import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.application.usecase.search_users.dto.SearchUsersQuery;
import com.banking.auth_service.application.usecase.search_users.dto.SearchUsersResult;

public class SearchUsersService implements SearchUsersUseCase {

    private final UserRepositoryPort users;

    public SearchUsersService(UserRepositoryPort users) {
        this.users = users;
    }

    @Override
    public SearchUsersResult search(SearchUsersQuery query) {
        String q = normalizeQuery(query.query());

        int limit = query.limit() <= 0 ? 50 : Math.min(query.limit(), 200);
        int offset = Math.max(query.offset(), 0);

        return new SearchUsersResult(users.searchByEmail(q, limit, offset));
    }

    private static String normalizeQuery(String query) {
        if (query == null) {
            throw new IllegalArgumentException("query is required");
        }
        String normalized = query.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("query is required");
        }
        return normalized;
    }
}
