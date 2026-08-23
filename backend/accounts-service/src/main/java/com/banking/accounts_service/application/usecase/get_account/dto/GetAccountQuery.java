package com.banking.accounts_service.application.usecase.get_account.dto;

import java.util.UUID;

public record GetAccountQuery(
        UUID accountId,
        UUID requesterUserId,
        boolean requesterIsAdmin
) {}
