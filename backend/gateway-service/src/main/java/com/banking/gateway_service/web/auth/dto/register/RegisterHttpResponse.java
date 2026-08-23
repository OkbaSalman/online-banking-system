package com.banking.gateway_service.web.auth.dto.register;

public record RegisterHttpResponse(String userId, boolean verificationRequired) {}