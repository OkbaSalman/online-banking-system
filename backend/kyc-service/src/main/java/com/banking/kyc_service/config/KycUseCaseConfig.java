package com.banking.kyc_service.config;

import com.banking.kyc_service.application.port.KycApplicationRepositoryPort;
import com.banking.kyc_service.application.port.KycDocumentRepositoryPort;
import com.banking.kyc_service.application.port.EmailNotificationPort;
import com.banking.kyc_service.application.port.KycEventPublisherPort;
import com.banking.kyc_service.application.usecase.document.createDocumentSlot.CreateDocumentSlotService;
import com.banking.kyc_service.application.usecase.document.createDocumentSlot.CreateDocumentSlotUseCase;
import com.banking.kyc_service.application.usecase.document.getDocument.GetDocumentService;
import com.banking.kyc_service.application.usecase.document.getDocument.GetDocumentUseCase;
import com.banking.kyc_service.application.usecase.document.listMyDocuments.ListMyDocumentsService;
import com.banking.kyc_service.application.usecase.document.listMyDocuments.ListMyDocumentsUseCase;
import com.banking.kyc_service.application.usecase.kyc.adminListApplications.AdminListApplicationsService;
import com.banking.kyc_service.application.usecase.kyc.adminListApplications.AdminListApplicationsUseCase;
import com.banking.kyc_service.application.usecase.kyc.adminListPending.AdminListPendingService;
import com.banking.kyc_service.application.usecase.kyc.adminListPending.AdminListPendingUseCase;
import com.banking.kyc_service.application.usecase.kyc.adminReview.AdminReviewService;
import com.banking.kyc_service.application.usecase.kyc.adminReview.AdminReviewUseCase;
import com.banking.kyc_service.application.usecase.kyc.getMyKyc.GetMyKycService;
import com.banking.kyc_service.application.usecase.kyc.getMyKyc.GetMyKycUseCase;
import com.banking.kyc_service.application.usecase.kyc.getOrCreateDraft.GetOrCreateDraftService;
import com.banking.kyc_service.application.usecase.kyc.getOrCreateDraft.GetOrCreateDraftUseCase;
import com.banking.kyc_service.application.usecase.kyc.submitKyc.SubmitKycService;
import com.banking.kyc_service.application.usecase.kyc.submitKyc.SubmitKycUseCase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KycUseCaseConfig {

    @Bean
    public GetOrCreateDraftUseCase getOrCreateDraftUseCase(KycApplicationRepositoryPort apps) {
        return new GetOrCreateDraftService(apps);
    }

    @Bean
    public GetMyKycUseCase getMyKycUseCase(KycApplicationRepositoryPort apps, GetOrCreateDraftUseCase draft) {
        return new GetMyKycService(apps, draft);
    }

    @Bean
    public SubmitKycUseCase submitKycUseCase(
            KycApplicationRepositoryPort apps,
            KycDocumentRepositoryPort docs,
            KycEventPublisherPort events
    ) {
        return new SubmitKycService(apps, docs, events);
    }

    @Bean
    public AdminListPendingUseCase adminListPendingUseCase(KycApplicationRepositoryPort apps) {
        return new AdminListPendingService(apps);
    }

    @Bean
    public AdminListApplicationsUseCase adminListApplicationsUseCase(KycApplicationRepositoryPort apps) {
        return new AdminListApplicationsService(apps);
    }

    @Bean
    public AdminReviewUseCase adminReviewUseCase(
            KycApplicationRepositoryPort apps,
            KycEventPublisherPort events,
            EmailNotificationPort emailNotifications
    ) {
        return new AdminReviewService(apps, events, emailNotifications);
    }

    @Bean
    public CreateDocumentSlotUseCase createDocumentSlotUseCase(KycDocumentRepositoryPort docs, KycApplicationRepositoryPort apps) {
        return new CreateDocumentSlotService(docs, apps);
    }

    @Bean
    public ListMyDocumentsUseCase listMyDocumentsUseCase(KycDocumentRepositoryPort docs) {
        return new ListMyDocumentsService(docs);
    }

    @Bean
    public GetDocumentUseCase getDocumentUseCase(KycDocumentRepositoryPort docs) {
        return new GetDocumentService(docs);
    }
}