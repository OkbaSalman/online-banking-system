package com.banking.auth_service.application.port;

import com.banking.auth_service.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);

    List<User> searchByEmail(String query, int limit, int offset);

    User save(User user);
    Optional<User> findById(UUID id);
}
