package com.banking.accounts_service.application.usecase.set_account_frozen.dto;

import java.util.UUID;

public record SetAccountFrozenCommand(
        UUID requesterUserId,
        boolean requesterIsAdmin,
        UUID accountId,
        boolean frozen
) {}
