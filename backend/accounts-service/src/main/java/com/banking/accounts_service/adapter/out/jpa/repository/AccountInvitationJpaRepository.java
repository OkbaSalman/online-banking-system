package com.banking.accounts_service.adapter.out.jpa.repository;

import com.banking.accounts_service.adapter.out.jpa.entity.AccountInvitationEntity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountInvitationJpaRepository extends JpaRepository<AccountInvitationEntity, UUID> {

    Optional<AccountInvitationEntity> findByAccountIdAndInvitedUserIdAndStatus(UUID accountId, UUID invitedUserId, String status);

    List<AccountInvitationEntity> findByInvitedUserIdAndStatus(UUID invitedUserId, String status, Pageable pageable);

    List<AccountInvitationEntity> findByAccountIdAndStatus(UUID accountId, String status, Pageable pageable);
}
