package com.banking.kyc_service.application.usecase.document.getDocument;

import com.banking.kyc_service.application.port.KycDocumentRepositoryPort;
import com.banking.kyc_service.domain.model.KycDocument;

import java.util.UUID;

public class GetDocumentService implements GetDocumentUseCase {
    private final KycDocumentRepositoryPort docs;

    public GetDocumentService(KycDocumentRepositoryPort docs) {
        this.docs = docs;
    }

    @Override
    public KycDocument getById(UUID documentId) {
        return docs.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }
}