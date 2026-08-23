package com.banking.kyc_service.application.usecase.document.listMyDocuments;


import com.banking.kyc_service.domain.model.KycDocument;

import java.util.List;
import java.util.UUID;

public interface ListMyDocumentsUseCase {
    List<KycDocument> listByUserId(UUID userId);

    List<KycDocument> listByApplicationId(UUID applicationId);
}