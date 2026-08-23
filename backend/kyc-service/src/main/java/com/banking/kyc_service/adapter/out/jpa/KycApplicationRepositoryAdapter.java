package com.banking.kyc_service.adapter.out.jpa;

import com.banking.kyc_service.adapter.out.jpa.repository.KycApplicationRepository;
import com.banking.kyc_service.application.port.KycApplicationRepositoryPort;
import com.banking.kyc_service.domain.KycApplicationEntity;
import com.banking.kyc_service.domain.model.KycApplication;
import com.banking.kyc_service.domain.model.KycStatus;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class KycApplicationRepositoryAdapter implements KycApplicationRepositoryPort {
    private final KycApplicationRepository repo;

    public KycApplicationRepositoryAdapter(KycApplicationRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<KycApplication> findByUserId(UUID userId) {
        return repo.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Optional<KycApplication> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public KycApplication save(KycApplication app) {
        return toDomain(repo.save(toEntity(app)));
    }

    @Override
    public List<KycApplication> findPending(int limit, int offset) {
        var page = repo.findAllByStatusOrderByCreatedAt(
                KycStatus.PENDING,
                PageRequest.of(offset / limit, limit)
        );
        return page.stream().map(this::toDomain).toList();
    }

    @Override
    public List<KycApplication> findByStatuses(List<KycStatus> statuses, int limit, int offset) {
        int safeLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        int safeOffset = Math.max(offset, 0);
        var page = repo.findAllByStatusInOrderByUpdatedAtDesc(
                statuses,
                PageRequest.of(safeOffset / safeLimit, safeLimit)
        );
        return page.stream().map(this::toDomain).toList();
    }

    private KycApplication toDomain(KycApplicationEntity e) {
        return new KycApplication(
                e.getId(),
                e.getUserId(),
                e.getStatus(),
                e.getFullName(),
                e.getNationalId(),
                e.getAddress(),
                e.getReviewerUserId(),
                e.getRejectionReason(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private KycApplicationEntity toEntity(KycApplication d) {
        return KycApplicationEntity.builder()
                .id(d.id())
                .userId(d.userId())
                .status(d.status())
                .fullName(d.fullName())
                .nationalId(d.nationalId())
                .address(d.address())
                .reviewerUserId(d.reviewerUserId())
                .rejectionReason(d.rejectionReason())
                .createdAt(d.createdAt())
                .updatedAt(d.updatedAt())
                .build();
    }
}
