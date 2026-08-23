package com.banking.accounts_service.application.usecase.list_my_accounts.dto;

import java.util.UUID;

public record ListMyAccountsQuery(UUID requesterUserId) {}
