package com.banking.accounts_service.application.usecase.list_account_members.dto;

import java.util.UUID;

public record ListAccountMembersQuery(
        UUID requesterUserId,
        boolean requesterIsAdmin,
        UUID accountId
) {}
