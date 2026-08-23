package com.banking.kyc_service.application.usecase.document.createDocumentSlot;

import com.banking.kyc_service.application.usecase.document.createDocumentSlot.dto.CreateDocumentSlotCommand;
import com.banking.kyc_service.domain.model.KycDocument;

public interface CreateDocumentSlotUseCase {
    KycDocument createSlot(CreateDocumentSlotCommand command);
}