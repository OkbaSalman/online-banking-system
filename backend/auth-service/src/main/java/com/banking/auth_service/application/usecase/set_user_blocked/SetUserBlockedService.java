package com.banking.auth_service.application.usecase.set_user_blocked;

import com.banking.auth_service.application.port.UserRepositoryPort;
import com.banking.auth_service.application.usecase.common.exception.ForbiddenException;
import com.banking.auth_service.application.usecase.common.exception.NotFoundException;
import com.banking.auth_service.application.usecase.set_user_blocked.dto.SetUserBlockedCommand;
import com.banking.auth_service.application.usecase.set_user_blocked.dto.SetUserBlockedResult;
import com.banking.auth_service.domain.model.User;

public class SetUserBlockedService implements SetUserBlockedUseCase {

    private final UserRepositoryPort users;

    public SetUserBlockedService(UserRepositoryPort users) {
        this.users = users;
    }

    @Override
    public SetUserBlockedResult setBlocked(SetUserBlockedCommand command) {
        validate(command);

        if (!command.requesterIsAdmin()) {
            throw new ForbiddenException("ADMIN role required");
        }

        User user = users.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        User updated = new User(
                user.id(),
                user.email(),
                user.passwordHash(),
                user.role(),
                user.emailVerified(),
                command.blocked()
        );

        users.save(updated);

        return new SetUserBlockedResult(updated.id(), updated.blocked());
    }

    private static void validate(SetUserBlockedCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command is required");
        }
        if (command.requesterUserId() == null) {
            throw new IllegalArgumentException("requesterUserId is required");
        }
        if (command.userId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
    }
}
