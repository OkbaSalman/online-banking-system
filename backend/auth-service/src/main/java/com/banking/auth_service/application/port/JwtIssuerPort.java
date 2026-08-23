package com.banking.auth_service.application.port;
 
import java.time.Duration;
import java.util.UUID;

import com.banking.auth_service.domain.model.Role;
 
public interface JwtIssuerPort {
    record JwtIssueResult(String token, long expiresInSeconds) {}
 
    JwtIssueResult issueAccessToken(UUID userId, Role role, Duration ttl);
}