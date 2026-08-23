package com.banking.auth_service.application.usecase.search_users.dto;

import com.banking.auth_service.domain.model.User;

import java.util.List;

public record SearchUsersResult(List<User> users) {}
