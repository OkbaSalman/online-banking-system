package com.banking.kyc_service.domain;

import java.time.Instant;
import java.util.UUID;

import com.banking.kyc_service.domain.model.DocumentType;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "kyc_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDocumentEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID applicationId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "document_type")
    private DocumentType type;

    @Column(nullable = false, length = 500)
    private String objectKey;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(nullable = false)
    private Instant uploadedAt;
}