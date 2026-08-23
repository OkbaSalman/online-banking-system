package com.banking.auth_service.adapter.out.jpa.repo;

import java.util.Optional;
import java.util.UUID;
 
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
 
import com.banking.auth_service.adapter.out.jpa.entity.UserEntity;

public interface UserSpringDataRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);

    List<UserEntity> findByEmailContainingIgnoreCase(String query, Pageable pageable);
}
