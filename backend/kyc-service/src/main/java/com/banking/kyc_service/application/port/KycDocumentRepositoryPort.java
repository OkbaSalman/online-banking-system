package com.banking.kyc_service.application.port;

import com.banking.kyc_service.domain.model.DocumentType;
import com.banking.kyc_service.domain.model.KycDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KycDocumentRepositoryPort {
    KycDocument save(KycDocument doc);
    Optional<KycDocument> findById(UUID id);
    List<KycDocument> findByUserId(UUID userId);
    List<KycDocument> findByApplicationId(UUID applicationId);
    List<KycDocument> findAllById(List<UUID> ids);
    List<KycDocument> findByApplicationIdAndType(UUID applicationId, DocumentType type);
}