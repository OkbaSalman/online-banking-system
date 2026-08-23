package com.banking.kyc_service.domain;

import java.time.Instant;
import java.util.UUID;

import com.banking.kyc_service.domain.model.KycStatus;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "kyc_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycApplicationEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status;

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String nationalId;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(columnDefinition = "uuid")
    private UUID reviewerUserId;

    @Column(length = 500)
    private String rejectionReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}