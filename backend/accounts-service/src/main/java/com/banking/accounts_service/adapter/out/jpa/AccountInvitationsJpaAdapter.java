package com.banking.accounts_service.adapter.out.jpa;

import com.banking.accounts_service.adapter.out.jpa.entity.AccountInvitationEntity;
import com.banking.accounts_service.adapter.out.jpa.repository.AccountInvitationJpaRepository;
import com.banking.accounts_service.application.port.AccountInvitationsRepositoryPort;
import com.banking.accounts_service.domain.model.AccountInvitation;
import com.banking.accounts_service.domain.model.AccountInvitationStatus;
import com.banking.accounts_service.domain.model.MembershipRole;

import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountInvitationsJpaAdapter implements AccountInvitationsRepositoryPort {

    private final AccountInvitationJpaRepository invitations;

    public AccountInvitationsJpaAdapter(AccountInvitationJpaRepository invitations) {
        this.invitations = invitations;
    }

    @Override
    public AccountInvitation save(AccountInvitation invitation) {
        return toDomain(invitations.save(toEntity(invitation)));
    }

    @Override
    public Optional<AccountInvitation> findById(UUID invitationId) {
        return invitations.findById(invitationId).map(this::toDomain);
    }

    @Override
    public Optional<AccountInvitation> findPendingByAccountIdAndInvitedUserId(UUID accountId, UUID invitedUserId) {
        return invitations.findByAccountIdAndInvitedUserIdAndStatus(accountId, invitedUserId, AccountInvitationStatus.PENDING.name())
                .map(this::toDomain);
    }

    @Override
    public List<AccountInvitation> listByInvitedUserId(UUID invitedUserId, AccountInvitationStatus status, int limit, int offset) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<AccountInvitationEntity> pageItems = invitations.findByInvitedUserIdAndStatus(
                invitedUserId,
                status.name(),
                PageRequest.of(page, safeLimit + offsetInPage)
        );

        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AccountInvitation> listByAccountId(UUID accountId, AccountInvitationStatus status, int limit, int offset) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);

        int page = safeOffset / safeLimit;
        int offsetInPage = safeOffset % safeLimit;

        List<AccountInvitationEntity> pageItems = invitations.findByAccountIdAndStatus(
                accountId,
                status.name(),
                PageRequest.of(page, safeLimit + offsetInPage)
        );

        if (offsetInPage >= pageItems.size()) {
            return List.of();
        }

        return pageItems.subList(offsetInPage, pageItems.size()).stream()
                .limit(safeLimit)
                .map(this::toDomain)
                .toList();
    }

    private AccountInvitation toDomain(AccountInvitationEntity e) {
        return new AccountInvitation(
                e.getId(),
                e.getAccountId(),
                e.getInvitedUserId(),
                e.getInvitedByUserId(),
                MembershipRole.valueOf(e.getRole()),
                AccountInvitationStatus.valueOf(e.getStatus()),
                e.getCreatedAtEpochMs(),
                e.getExpiresAtEpochMs(),
                e.getRespondedAtEpochMs(),
                e.getInvitedByEmail()
        );
    }

    private AccountInvitationEntity toEntity(AccountInvitation d) {
        return new AccountInvitationEntity(
                d.id(),
                d.accountId(),
                d.invitedUserId(),
                d.invitedByUserId(),
                d.role().name(),
                d.status().name(),
                d.createdAtEpochMs(),
                d.expiresAtEpochMs(),
                d.respondedAtEpochMs(),
                d.invitedByEmail()
        );
    }
}
