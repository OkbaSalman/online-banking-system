package com.banking.auth_service.application.usecase.verify_email.dto;

public record VerifyEmailCommand(String email, String code) {}