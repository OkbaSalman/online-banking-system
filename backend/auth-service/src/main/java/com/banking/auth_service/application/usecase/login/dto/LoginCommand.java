package com.banking.auth_service.application.usecase.login.dto;

public record LoginCommand(String email, String password) {}