package com.banking.auth_service.adapter.out.jpa.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {
 
    @Id
    public UUID id;
 
    @Column(nullable = false, unique = true)
    public String email;
 
    @Column(name = "password_hash", nullable = false)
    public String passwordHash;
 
    @Column(nullable = false)
    public String role;
 
    @Column(name = "email_verified", nullable = false)
    public boolean emailVerified;

    @Column(name = "blocked", nullable = false)
    public boolean blocked;
 
    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;
}
