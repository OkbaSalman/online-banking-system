package com.banking.accounts_service.application.port;

import com.banking.accounts_service.domain.model.AccountInvitation;
import com.banking.accounts_service.domain.model.AccountInvitationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountInvitationsRepositoryPort {

    AccountInvitation save(AccountInvitation invitation);

    Optional<AccountInvitation> findById(UUID invitationId);

    Optional<AccountInvitation> findPendingByAccountIdAndInvitedUserId(UUID accountId, UUID invitedUserId);

    List<AccountInvitation> listByInvitedUserId(UUID invitedUserId, AccountInvitationStatus status, int limit, int offset);

    List<AccountInvitation> listByAccountId(UUID accountId, AccountInvitationStatus status, int limit, int offset);
}
