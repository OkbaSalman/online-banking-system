package com.banking.kyc_service.application.usecase.document.getDocument;

import com.banking.kyc_service.domain.model.KycDocument;

import java.util.UUID;

public interface GetDocumentUseCase {
    KycDocument getById(UUID documentId);
}