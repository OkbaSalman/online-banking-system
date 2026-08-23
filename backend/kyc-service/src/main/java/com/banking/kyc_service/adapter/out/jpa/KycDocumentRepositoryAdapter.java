package com.banking.kyc_service.adapter.out.jpa;

import com.banking.kyc_service.adapter.out.jpa.repository.KycDocumentRepository;
import com.banking.kyc_service.application.port.KycDocumentRepositoryPort;
import com.banking.kyc_service.domain.KycDocumentEntity;
import com.banking.kyc_service.domain.model.DocumentType;
import com.banking.kyc_service.domain.model.KycDocument;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class KycDocumentRepositoryAdapter implements KycDocumentRepositoryPort {
    private final KycDocumentRepository repo;

    public KycDocumentRepositoryAdapter(KycDocumentRepository repo) {
        this.repo = repo;
    }

    @Override
    public KycDocument save(KycDocument doc) {
        return toDomain(repo.save(toEntity(doc)));
    }

    @Override
    public Optional<KycDocument> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<KycDocument> findByUserId(UUID userId) {
        return repo.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<KycDocument> findByApplicationId(UUID applicationId) {
        return repo.findByApplicationId(applicationId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<KycDocument> findAllById(List<UUID> ids) {
        return repo.findAllById(ids).stream().map(this::toDomain).toList();
    }

    @Override
    public List<KycDocument> findByApplicationIdAndType(UUID applicationId, DocumentType type) {
        return repo.findByApplicationIdAndType(applicationId, type).stream().map(this::toDomain).toList();
    }

    private KycDocument toDomain(KycDocumentEntity e) {
        return new KycDocument(
                e.getId(),
                e.getApplicationId(),
                e.getUserId(),
                e.getType(),
                e.getObjectKey(),
                e.getOriginalFilename(),
                e.getContentType(),
                e.getSizeBytes(),
                e.getSha256(),
                e.getUploadedAt()
        );
    }

    private KycDocumentEntity toEntity(KycDocument d) {
        return KycDocumentEntity.builder()
                .id(d.id())
                .applicationId(d.applicationId())
                .userId(d.userId())
                .type(d.type())
                .objectKey(d.objectKey())
                .originalFilename(d.originalFilename())
                .contentType(d.contentType())
                .sizeBytes(d.sizeBytes())
                .sha256(d.sha256())
                .uploadedAt(d.uploadedAt())
                .build();
    }
}