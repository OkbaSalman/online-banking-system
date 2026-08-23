package com.banking.gateway_service.web.auth.dto.verify;

public record VerifyEmailHttpRequest(String email, String code) {}