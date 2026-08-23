package com.banking.auth_service.domain.model;
import java.util.UUID;
 

public record User (
    UUID id,
    String email,
    String passwordHash,
    Role role,
    boolean emailVerified,
    boolean blocked
){}
