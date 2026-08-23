package com.banking.kyc_service.adapter.out.jpa.repository;

import com.banking.kyc_service.domain.KycApplicationEntity;
import com.banking.kyc_service.domain.model.KycStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KycApplicationRepository extends JpaRepository<KycApplicationEntity, UUID> {
    Optional<KycApplicationEntity> findByUserId(UUID userId);
    boolean existsByUserIdAndStatusNot(UUID userId, KycStatus status);
    List<KycApplicationEntity> findAllByStatusOrderByCreatedAt(KycStatus status, org.springframework.data.domain.Pageable pageable);

    List<KycApplicationEntity> findAllByStatusInOrderByUpdatedAtDesc(List<KycStatus> statuses, org.springframework.data.domain.Pageable pageable);
}