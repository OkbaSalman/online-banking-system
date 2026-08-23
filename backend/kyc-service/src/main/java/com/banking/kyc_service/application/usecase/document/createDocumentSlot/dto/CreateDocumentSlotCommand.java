package com.banking.kyc_service.application.usecase.document.createDocumentSlot.dto;

import com.banking.kyc_service.domain.model.DocumentType;

import java.util.UUID;

public record CreateDocumentSlotCommand(
        UUID userId,
        DocumentType type,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String sha256
) {}
