package com.banking.kyc_service.adapter.out.jpa.repository;

import com.banking.kyc_service.domain.KycDocumentEntity;
import com.banking.kyc_service.domain.model.DocumentType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocumentEntity, UUID> {
    List<KycDocumentEntity> findByApplicationId(UUID applicationId);
    List<KycDocumentEntity> findByUserId(UUID userId);
    List<KycDocumentEntity> findByApplicationIdAndType(UUID applicationId, DocumentType type);
}