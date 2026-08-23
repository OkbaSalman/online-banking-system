package com.banking.gateway_service.web.accounts.dto.member;

public record IsMemberHttpResponse(
        boolean isMember,
        String role
) {}
