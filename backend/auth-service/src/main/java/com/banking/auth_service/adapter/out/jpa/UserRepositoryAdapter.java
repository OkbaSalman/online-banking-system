package com.banking.auth_service.adapter.out.jpa;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
 
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
 
import com.banking.auth_service.adapter.out.jpa.entity.UserEntity;
import com.banking.auth_service.adapter.out.jpa.repo.UserSpringDataRepository;
import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.domain.model.Role;
import com.banking.auth_service.domain.model.User;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
 
    private final UserSpringDataRepository repo;
 
    public UserRepositoryAdapter(UserSpringDataRepository repo) {
        this.repo = repo;
    }
 
    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email).map(this::toDomain);
    }
 
    @Override
    public User save(User user) {
        UserEntity e = toEntity(user);
        if (e.createdAt == null) {
            e.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
        return toDomain(repo.save(e));
    }
 
    @Override
    public Optional<User> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<User> searchByEmail(String query, int limit, int offset) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<UserEntity> pageItems = repo.findByEmailContainingIgnoreCase(query, PageRequest.of(page, safeLimit + offsetInPage));
        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }
        return pageItems.subList(offsetInPage, pageItems.size()).stream().limit(safeLimit).map(this::toDomain).toList();
    }
 
    private User toDomain(UserEntity e) {
        return new User(
                e.id,
                e.email,
                e.passwordHash,
                Role.valueOf(e.role),
                e.emailVerified,
                e.blocked
        );
    }
 
    private UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.id = u.id();
        e.email = u.email();
        e.passwordHash = u.passwordHash();
        e.role = u.role().name();
        e.emailVerified = u.emailVerified();
        e.blocked = u.blocked();
        return e;
    }
}