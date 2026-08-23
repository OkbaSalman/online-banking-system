package com.banking.auth_service.application.usecase.register.dto;

import java.util.UUID;


public record RegisterUserResult(UUID userId, boolean verificationRequired) {}