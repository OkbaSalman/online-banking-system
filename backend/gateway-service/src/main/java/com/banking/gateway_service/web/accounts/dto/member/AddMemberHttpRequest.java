package com.banking.gateway_service.web.accounts.dto.member;

public record AddMemberHttpRequest(
        String memberUserId,
        String role
) {}
