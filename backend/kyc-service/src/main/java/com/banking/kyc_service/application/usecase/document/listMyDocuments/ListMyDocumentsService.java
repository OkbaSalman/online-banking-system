package com.banking.kyc_service.application.usecase.document.listMyDocuments;

import com.banking.kyc_service.application.port.KycDocumentRepositoryPort;
import com.banking.kyc_service.domain.model.KycDocument;

import java.util.List;
import java.util.UUID;

public class ListMyDocumentsService implements ListMyDocumentsUseCase {
    private final KycDocumentRepositoryPort docs;

    public ListMyDocumentsService(KycDocumentRepositoryPort docs) {
        this.docs = docs;
    }

    @Override
    public List<KycDocument> listByUserId(UUID userId) {
        return docs.findByUserId(userId);
    }

    @Override
    public List<KycDocument> listByApplicationId(UUID applicationId) {
        return docs.findByApplicationId(applicationId);
    }
}
