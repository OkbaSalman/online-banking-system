package com.banking.accounts_service.application.usecase.can_debit.dto;

import java.util.UUID;

public record CanDebitQuery(
        UUID requesterUserId,
        boolean requesterIsAdmin,
        UUID accountId,
        UUID userId
) {}
