package com.banking.auth_service.application.usecase.reset_password.dto;

public record ResetPasswordCommand(String token, String newPassword) {}
