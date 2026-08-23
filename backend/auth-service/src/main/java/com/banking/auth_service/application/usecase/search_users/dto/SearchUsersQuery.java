package com.banking.auth_service.application.usecase.search_users.dto;

public record SearchUsersQuery(String query, int limit, int offset) {}
