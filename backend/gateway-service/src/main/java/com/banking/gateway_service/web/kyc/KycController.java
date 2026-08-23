package com.banking.gateway_service.web.kyc;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banking.gateway_service.grpc.security.GrpcAuthContext;
import com.banking.gateway_service.web.kyc.dto.application.AdminListPendingHttpResponse;
import com.banking.gateway_service.web.kyc.dto.application.AdminReviewHttpRequest;
import com.banking.gateway_service.web.kyc.dto.application.AdminReviewHttpResponse;
import com.banking.gateway_service.web.kyc.dto.application.GetMyKycHttpResponse;
import com.banking.gateway_service.web.kyc.dto.application.KycApplicationHttpDto;
import com.banking.gateway_service.web.kyc.dto.document.CreateDocumentSlotHttpRequest;
import com.banking.gateway_service.web.kyc.dto.document.CreateDocumentSlotHttpResponse;
import com.banking.gateway_service.web.kyc.dto.document.GetDocumentDownloadUrlHttpResponse;
import com.banking.gateway_service.web.kyc.dto.document.KycDocumentHttpDto;
import com.banking.gateway_service.web.kyc.dto.document.ListMyDocumentsHttpResponse;
import com.banking.gateway_service.web.kyc.dto.submit.SubmitKycHttpRequest;
import com.banking.gateway_service.web.kyc.dto.submit.SubmitKycHttpResponse;
import com.banking.kyc.v1.AdminGetDocumentDownloadUrlRequest;
import com.banking.kyc.v1.AdminListApplicationsRequest;
import com.banking.kyc.v1.AdminListDocumentsForApplicationRequest;
import com.banking.kyc.v1.AdminListPendingRequest;
import com.banking.kyc.v1.AdminReviewRequest;
import com.banking.kyc.v1.CreateDocumentSlotRequest;
import com.banking.kyc.v1.DocumentType;
import com.banking.kyc.v1.GetDocumentDownloadUrlRequest;
import com.banking.kyc.v1.GetMyKycRequest;
import com.banking.kyc.v1.KycApplication;
import com.banking.kyc.v1.KycDocument;
import com.banking.kyc.v1.KycServiceGrpc;
import com.banking.kyc.v1.KycStatus;
import com.banking.kyc.v1.ListMyDocumentsRequest;
import com.banking.kyc.v1.PingRequest;
import com.banking.kyc.v1.SubmitKycRequest;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class KycController {

    private final KycServiceGrpc.KycServiceBlockingStub kyc;

    public record PingHttpResponse(String message) {}

    public KycController(KycServiceGrpc.KycServiceBlockingStub kyc) {
        this.kyc = kyc;
    }

    @GetMapping("/api/kyc/ping")
    public Mono<PingHttpResponse> ping(Authentication authentication, @RequestParam(defaultValue = "hello") String message) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> kyc.ping(PingRequest.newBuilder().setMessage(message).build())))
                .subscribeOn(Schedulers.boundedElastic())
                .map(res -> new PingHttpResponse(res.getMessage()));
    }

    @PostMapping("/api/kyc/documents/slots")
    public Mono<CreateDocumentSlotHttpResponse> createDocumentSlot(Authentication authentication, @RequestBody CreateDocumentSlotHttpRequest body) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> kyc.createDocumentSlot(
                CreateDocumentSlotRequest.newBuilder()
                        .setType(parseDocumentType(body.type()))
                        .setOriginalFilename(body.originalFilename())
                        .setContentType(body.contentType())
                        .setSizeBytes(body.sizeBytes())
                        .setSha256(body.sha256())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new CreateDocumentSlotHttpResponse(toHttp(res.getDocument()), res.getUploadUrl()));
    }

    @GetMapping("/api/kyc/documents")
    public Mono<ListMyDocumentsHttpResponse> listMyDocuments(Authentication authentication) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> kyc.listMyDocuments(
                ListMyDocumentsRequest.newBuilder().build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListMyDocumentsHttpResponse(res.getDocumentsList().stream().map(KycController::toHttp).toList()));
    }

    @GetMapping("/api/kyc/documents/{documentId}/download-url")
    public Mono<GetDocumentDownloadUrlHttpResponse> getDocumentDownloadUrl(Authentication authentication, @PathVariable String documentId) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> kyc.getDocumentDownloadUrl(
                GetDocumentDownloadUrlRequest.newBuilder().setDocumentId(documentId).build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new GetDocumentDownloadUrlHttpResponse(res.getDownloadUrl()));
    }

    @PostMapping("/api/kyc/submit")
    public Mono<SubmitKycHttpResponse> submitKyc(Authentication authentication, @RequestBody SubmitKycHttpRequest body) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> kyc.submitKyc(
                SubmitKycRequest.newBuilder()
                        .setFullName(body.fullName())
                        .setNationalId(body.nationalId())
                        .setAddress(body.address())
                        .addAllDocumentIds(body.documentIds())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new SubmitKycHttpResponse(toHttp(res.getApplication())));
    }

    @GetMapping("/api/kyc/me")
    public Mono<GetMyKycHttpResponse> getMyKyc(Authentication authentication) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> kyc.getMyKyc(
                GetMyKycRequest.newBuilder().build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new GetMyKycHttpResponse(toHttp(res.getApplication())));
    }

    @GetMapping("/api/kyc/admin/pending")
    public Mono<AdminListPendingHttpResponse> adminListPending(
            Authentication authentication,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> kyc.adminListPending(
                AdminListPendingRequest.newBuilder()
                        .setLimit(limit)
                        .setOffset(offset)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new AdminListPendingHttpResponse(res.getApplicationsList().stream().map(KycController::toHttp).toList()));
    }

    @GetMapping("/api/kyc/admin/applications")
    public Mono<AdminListPendingHttpResponse> adminListApplications(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> kyc.adminListApplications(
                AdminListApplicationsRequest.newBuilder()
                        .setStatus(parseKycStatus(status))
                        .setLimit(limit)
                        .setOffset(offset)
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new AdminListPendingHttpResponse(res.getApplicationsList().stream().map(KycController::toHttp).toList()));
    }

    @PostMapping("/api/kyc/admin/applications/{applicationId}/review")
    public Mono<AdminReviewHttpResponse> adminReview(
            Authentication authentication,
            @PathVariable String applicationId,
            @RequestBody AdminReviewHttpRequest body
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () -> kyc.adminReview(
                AdminReviewRequest.newBuilder()
                        .setApplicationId(applicationId)
                        .setApprove(body.approve())
                        .setRejectionReason(body.rejectionReason() == null ? "" : body.rejectionReason())
                        .build()
        ))).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new AdminReviewHttpResponse(toHttp(res.getApplication())));
    }

    @GetMapping("/api/kyc/admin/applications/{applicationId}/documents")
    public Mono<ListMyDocumentsHttpResponse> adminListDocumentsForApplication(
            Authentication authentication,
            @PathVariable String applicationId
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () ->
                kyc.adminListDocumentsForApplication(
                        AdminListDocumentsForApplicationRequest.newBuilder()
                                .setApplicationId(applicationId)
                                .build()
                )
        )).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new ListMyDocumentsHttpResponse(res.getDocumentsList().stream().map(KycController::toHttp).toList()));
    }

    @GetMapping("/api/kyc/admin/documents/{documentId}/download-url")
    public Mono<GetDocumentDownloadUrlHttpResponse> adminGetDocumentDownloadUrl(
            Authentication authentication,
            @PathVariable String documentId
    ) {
        return Mono.fromCallable(() -> GrpcAuthContext.callWithAuth(authentication, () ->
                kyc.adminGetDocumentDownloadUrl(
                        AdminGetDocumentDownloadUrlRequest.newBuilder()
                                .setDocumentId(documentId)
                                .build()
                )
        )).subscribeOn(Schedulers.boundedElastic())
          .map(res -> new GetDocumentDownloadUrlHttpResponse(res.getDownloadUrl()));
    }

    private static KycStatus parseKycStatus(String status) {
        if (status == null || status.isBlank()) {
            return KycStatus.KYC_STATUS_UNSPECIFIED;
        }
        String v = status.trim().toUpperCase();
        return switch (v) {
            case "PENDING", "KYC_STATUS_PENDING" -> KycStatus.KYC_STATUS_PENDING;
            case "APPROVED", "KYC_STATUS_APPROVED" -> KycStatus.KYC_STATUS_APPROVED;
            case "REJECTED", "KYC_STATUS_REJECTED" -> KycStatus.KYC_STATUS_REJECTED;
            case "REVIEWED", "COMPLETED", "DONE" -> KycStatus.KYC_STATUS_UNSPECIFIED;
            default -> KycStatus.KYC_STATUS_UNSPECIFIED;
        };
    }

    private static DocumentType parseDocumentType(String type) {
        if (type == null) {
            return DocumentType.DOCUMENT_TYPE_UNSPECIFIED;
        }
        String v = type.trim().toUpperCase();
        return switch (v) {
            case "ID_FRONT", "DOCUMENT_TYPE_ID_FRONT" -> DocumentType.DOCUMENT_TYPE_ID_FRONT;
            case "ID_BACK", "DOCUMENT_TYPE_ID_BACK" -> DocumentType.DOCUMENT_TYPE_ID_BACK;
            case "PROOF_ADDRESS", "DOCUMENT_TYPE_PROOF_ADDRESS" -> DocumentType.DOCUMENT_TYPE_PROOF_ADDRESS;
            case "SELFIE", "DOCUMENT_TYPE_SELFIE" -> DocumentType.DOCUMENT_TYPE_SELFIE;
            default -> DocumentType.DOCUMENT_TYPE_UNSPECIFIED;
        };
    }

    private static KycDocumentHttpDto toHttp(KycDocument d) {
        return new KycDocumentHttpDto(
                d.getId(),
                d.getApplicationId(),
                d.getUserId(),
                d.getType().name(),
                d.getObjectKey(),
                d.getOriginalFilename(),
                d.getContentType(),
                d.getSizeBytes(),
                d.getSha256(),
                d.getUploadedAtEpochMs()
        );
    }

    private static KycApplicationHttpDto toHttp(KycApplication a) {
        return new KycApplicationHttpDto(
                a.getId(),
                a.getUserId(),
                a.getStatus().name(),
                a.getFullName(),
                a.getNationalId(),
                a.getAddress(),
                a.getReviewerUserId(),
                a.getRejectionReason(),
                a.getCreatedAtEpochMs(),
                a.getUpdatedAtEpochMs()
        );
    }
}