package com.banking.kyc_service.application.usecase.document.createDocumentSlot;

import com.banking.kyc_service.application.port.KycApplicationRepositoryPort;
import com.banking.kyc_service.application.port.KycDocumentRepositoryPort;
import com.banking.kyc_service.application.usecase.document.createDocumentSlot.dto.CreateDocumentSlotCommand;
import com.banking.kyc_service.domain.model.KycApplication;
import com.banking.kyc_service.domain.model.KycDocument;
import com.banking.kyc_service.domain.model.KycStatus;

import java.time.Instant;
import java.util.UUID;

public class CreateDocumentSlotService implements CreateDocumentSlotUseCase {
    private final KycDocumentRepositoryPort docs;
    private final KycApplicationRepositoryPort apps;

    public CreateDocumentSlotService(KycDocumentRepositoryPort docs, KycApplicationRepositoryPort apps) {
        this.docs = docs;
        this.apps = apps;
    }

    @Override
    public KycDocument createSlot(CreateDocumentSlotCommand command) {
        KycApplication draft = apps.findByUserId(command.userId()).orElseGet(() -> {
            Instant now = Instant.now();
            KycApplication created = new KycApplication(
                    UUID.randomUUID(),
                    command.userId(),
                    KycStatus.NOT_SUBMITTED,
                    "",
                    "",
                    "",
                    null,
                    null,
                    now,
                    now
            );
            return apps.save(created);
        });

        String objectKey = String.format(
                "kyc/%s/%s/%s/%s.%s",
                command.userId(),
                draft.id(),
                command.type().name().toLowerCase(),
                UUID.randomUUID(),
                getFileExtension(command.originalFilename())
        );

        KycDocument doc = new KycDocument(
                UUID.randomUUID(),
                draft.id(),
                command.userId(),
                command.type(),
                objectKey,
                command.originalFilename(),
                command.contentType(),
                command.sizeBytes(),
                command.sha256(),
                Instant.now()
        );

        return docs.save(doc);
    }

    private static String getFileExtension(String filename) {
        if (filename == null) {
            return "bin";
        }
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i + 1) : "bin";
    }
}