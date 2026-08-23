package com.banking.kyc_service.adapter.in.grpc;

import com.banking.kyc.v1.*;
import com.banking.kyc_service.adapter.in.grpc.security.AuthMetadataServerInterceptor;
import com.banking.kyc_service.application.usecase.document.createDocumentSlot.CreateDocumentSlotUseCase;
import com.banking.kyc_service.application.usecase.document.createDocumentSlot.dto.CreateDocumentSlotCommand;
import com.banking.kyc_service.application.usecase.document.getDocument.GetDocumentUseCase;
import com.banking.kyc_service.application.usecase.document.listMyDocuments.ListMyDocumentsUseCase;

import com.banking.kyc_service.application.usecase.kyc.adminListApplications.AdminListApplicationsUseCase;
import com.banking.kyc_service.application.usecase.kyc.adminListPending.AdminListPendingUseCase;
import com.banking.kyc_service.application.usecase.kyc.adminReview.AdminReviewUseCase;
import com.banking.kyc_service.application.usecase.kyc.adminReview.dto.AdminReviewCommand;
import com.banking.kyc_service.application.usecase.kyc.getMyKyc.GetMyKycUseCase;
import com.banking.kyc_service.application.usecase.kyc.submitKyc.SubmitKycUseCase;
import com.banking.kyc_service.application.usecase.kyc.submitKyc.dto.SubmitKycCommand;

import com.banking.kyc_service.domain.model.KycApplication;
import com.banking.kyc_service.domain.model.KycDocument;

import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KycGrpcController extends KycServiceGrpc.KycServiceImplBase {

    private final GetMyKycUseCase getMyKycUseCase;
    private final SubmitKycUseCase submitKycUseCase;
    private final AdminListPendingUseCase adminListPendingUseCase;
    private final AdminListApplicationsUseCase adminListApplicationsUseCase;
    private final AdminReviewUseCase adminReviewUseCase;

    private final CreateDocumentSlotUseCase createDocumentSlotUseCase;
    private final ListMyDocumentsUseCase listMyDocumentsUseCase;
    private final GetDocumentUseCase getDocumentUseCase;

    private final String bucketName;
    private final MinioClient minio;
    private final int presignExpirySeconds;

    public KycGrpcController(
            GetMyKycUseCase getMyKycUseCase,
            SubmitKycUseCase submitKycUseCase,
            AdminListPendingUseCase adminListPendingUseCase,
            AdminListApplicationsUseCase adminListApplicationsUseCase,
            AdminReviewUseCase adminReviewUseCase,
            CreateDocumentSlotUseCase createDocumentSlotUseCase,
            ListMyDocumentsUseCase listMyDocumentsUseCase,
            GetDocumentUseCase getDocumentUseCase,
            @Value("${minio.bucket}") String bucketName,
            MinioClient minio,
            @Value("${minio.presign.expiry-seconds:600}") int presignExpirySeconds
    ) {
        this.getMyKycUseCase = getMyKycUseCase;
        this.submitKycUseCase = submitKycUseCase;
        this.adminListPendingUseCase = adminListPendingUseCase;
        this.adminListApplicationsUseCase = adminListApplicationsUseCase;
        this.adminReviewUseCase = adminReviewUseCase;
        this.createDocumentSlotUseCase = createDocumentSlotUseCase;
        this.listMyDocumentsUseCase = listMyDocumentsUseCase;
        this.getDocumentUseCase = getDocumentUseCase;
        this.bucketName = bucketName;
        this.minio = minio;
        this.presignExpirySeconds = presignExpirySeconds;
    }

    private String presignedPutUrl(String objectKey) throws Exception {
        return minio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucketName)
                        .object(objectKey)
                        .expiry(presignExpirySeconds)
                        .build()
        );
    }

    private String presignedGetUrl(String objectKey) throws Exception {
        return minio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectKey)
                        .expiry(presignExpirySeconds)
                        .build()
        );
    }

    private UUID currentUserId() {
        String uid = AuthMetadataServerInterceptor.USER_ID.get(Context.current());
        if (uid == null || uid.isBlank()) {
            throw Status.UNAUTHENTICATED.withDescription("Missing x-user-id").asRuntimeException();
        }
        try {
            return UUID.fromString(uid);
        } catch (IllegalArgumentException ex) {
            throw Status.INVALID_ARGUMENT.withDescription("Invalid x-user-id").withCause(ex).asRuntimeException();
        }
    }

    private String currentRole() {
        String role = AuthMetadataServerInterceptor.ROLE.get(Context.current());
        return role != null ? role : "USER";
    }

    private void requireAdmin() {
        if (!"ADMIN".equals(currentRole())) {
            throw Status.PERMISSION_DENIED.asRuntimeException();
        }
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        responseObserver.onNext(PingResponse.newBuilder().setMessage("pong: " + request.getMessage()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void createDocumentSlot(CreateDocumentSlotRequest request,
                                   StreamObserver<CreateDocumentSlotResponse> responseObserver) {
        try {
            UUID userId = currentUserId();

            com.banking.kyc_service.domain.model.DocumentType type = switch (request.getType()) {
                case DOCUMENT_TYPE_ID_FRONT -> com.banking.kyc_service.domain.model.DocumentType.ID_FRONT;
                case DOCUMENT_TYPE_ID_BACK -> com.banking.kyc_service.domain.model.DocumentType.ID_BACK;
                case DOCUMENT_TYPE_PROOF_ADDRESS -> com.banking.kyc_service.domain.model.DocumentType.PROOF_ADDRESS;
                case DOCUMENT_TYPE_SELFIE -> com.banking.kyc_service.domain.model.DocumentType.SELFIE;
                default -> throw Status.INVALID_ARGUMENT.withDescription("Invalid document type").asRuntimeException();
            };

            KycDocument doc = createDocumentSlotUseCase.createSlot(new CreateDocumentSlotCommand(
                    userId,
                    type,
                    request.getOriginalFilename(),
                    request.getContentType(),
                    request.getSizeBytes(),
                    request.getSha256()
            ));

            String uploadUrl = presignedPutUrl(doc.objectKey());

            responseObserver.onNext(CreateDocumentSlotResponse.newBuilder()
                    .setDocument(toProto(doc))
                    .setUploadUrl(uploadUrl)
                    .build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(GrpcErrorMapper.map(ex));
        }
    }

    @Override
    public void listMyDocuments(ListMyDocumentsRequest request,
                                StreamObserver<ListMyDocumentsResponse> responseObserver) {
        try {
            UUID userId = currentUserId();
            List<KycDocument> docs = listMyDocumentsUseCase.listByUserId(userId);

            var protoDocs = docs.stream().map(this::toProto).collect(Collectors.toList());
            responseObserver.onNext(ListMyDocumentsResponse.newBuilder().addAllDocuments(protoDocs).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(GrpcErrorMapper.map(ex));
        }
    }

    @Override
    public void getDocumentDownloadUrl(GetDocumentDownloadUrlRequest request,
                                       StreamObserver<GetDocumentDownloadUrlResponse> responseObserver) {
        try {
            UUID userId = currentUserId();
            UUID docId = UUID.fromString(request.getDocumentId());

            KycDocument doc = getDocumentUseCase.getById(docId);
            if (!doc.userId().equals(userId)) {
                throw Status.PERMISSION_DENIED.asRuntimeException();
            }

            String downloadUrl = presignedGetUrl(doc.objectKey());
            responseObserver.onNext(GetDocumentDownloadUrlResponse.newBuilder().setDownloadUrl(downloadUrl).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(GrpcErrorMapper.map(ex));
        }
    }

    @Override
    public void submitKyc(SubmitKycRequest request,
                          StreamObserver<SubmitKycResponse> responseObserver) {
        try {
            UUID userId = currentUserId();
            List<UUID> docIds = request.getDocumentIdsList()
                    .stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

            KycApplication app = submitKycUseCase.submit(new SubmitKycCommand(
                    userId,
                    request.getFullName(),
                    request.getNationalId(),
                    request.getAddress(),
                    docIds
            ));

            responseObserver.onNext(SubmitKycResponse.newBuilder().setApplication(toProto(app)).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(GrpcErrorMapper.map(ex));
        }
    }

    @Override
    public void getMyKyc(GetMyKycRequest request,
                         StreamObserver<GetMyKycResponse> responseObserver) {
        try {
            UUID userId = currentUserId();
            KycApplication app = getMyKycUseCase.getMyKyc(userId);

            responseObserver.onNext(GetMyKycResponse.newBuilder().setApplication(toProto(app)).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(GrpcErrorMapper.map(ex));
        }
    }

    @Override
    public void adminListPending(AdminListPendingRequest request,StreamObserver<AdminListPendingResponse> responseObserver) {
        try {
            requireAdmin();
            List<KycApplication> apps = adminListPendingUseCase.listPending(request.getLimit(), request.getOffset());
            var protoApps = apps.stream().map(this::toProto).collect(Collectors.toList());

            responseObserver.onNext(AdminListPendingResponse.newBuilder().addAllApplications(protoApps).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(GrpcErrorMapper.map(ex));
        }
    }

    @Override
    public void adminListApplications(
            AdminListApplicationsRequest request,
            StreamObserver<AdminListApplicationsResponse> responseObserver
    ) {
        try {
            requireAdmin();
            com.banking.kyc_service.domain.model.KycStatus status = switch (request.getStatus()) {
                case KYC_STATUS_PENDING -> com.banking.kyc_service.domain.model.KycStatus.PENDING;
                case KYC_STATUS_APPROVED -> com.banking.kyc_service.domain.model.KycStatus.APPROVED;
                case KYC_STATUS_REJECTED -> com.banking.kyc_service.domain.model.KycStatus.REJECTED;
                default -> null;
            };
            List<KycApplication> apps = adminListApplicationsUseCase.list(status, request.getLimit(), request.getOffset());
            var protoApps = apps.stream().map(this::toProto).collect(Collectors.toList());
            responseObserver.onNext(AdminListApplicationsResponse.newBuilder().addAllApplications(protoApps).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(GrpcErrorMapper.map(ex));
        }
    }

    @Override
    public void adminReview(AdminReviewRequest request,
                            StreamObserver<AdminReviewResponse> responseObserver) {
        try {
            requireAdmin();
            UUID reviewerId = currentUserId();

            KycApplication app = adminReviewUseCase.review(new AdminReviewCommand(
                    reviewerId,
                    UUID.fromString(request.getApplicationId()),
                    request.getApprove(),
                    request.getRejectionReason()
            ));

            responseObserver.onNext(AdminReviewResponse.newBuilder().setApplication(toProto(app)).build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(GrpcErrorMapper.map(ex));
        }
    }

    @Override
    public void adminListDocumentsForApplication(
            AdminListDocumentsForApplicationRequest request,
            StreamObserver<AdminListDocumentsForApplicationResponse> responseObserver
    ) {
        try {
            requireAdmin();
            UUID applicationId = UUID.fromString(request.getApplicationId());
            List<KycDocument> docs = listMyDocumentsUseCase.listByApplicationId(applicationId);
            var protoDocs = docs.stream().map(this::toProto).collect(Collectors.toList());
            responseObserver.onNext(
                    AdminListDocumentsForApplicationResponse.newBuilder().addAllDocuments(protoDocs).build()
            );
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(GrpcErrorMapper.map(ex));
        }
    }

    @Override
    public void adminGetDocumentDownloadUrl(
            AdminGetDocumentDownloadUrlRequest request,
            StreamObserver<AdminGetDocumentDownloadUrlResponse> responseObserver
    ) {
        try {
            requireAdmin();
            UUID docId = UUID.fromString(request.getDocumentId());
            KycDocument doc = getDocumentUseCase.getById(docId);
            String downloadUrl = presignedGetUrl(doc.objectKey());
            responseObserver.onNext(
                    AdminGetDocumentDownloadUrlResponse.newBuilder().setDownloadUrl(downloadUrl).build()
            );
            responseObserver.onCompleted();
        } catch (Exception ex) {
            responseObserver.onError(GrpcErrorMapper.map(ex));
        }
    }

    private com.banking.kyc.v1.KycApplication toProto(com.banking.kyc_service.domain.model.KycApplication a) {
    return com.banking.kyc.v1.KycApplication.newBuilder()
            .setId(a.id().toString())
            .setUserId(a.userId().toString())
            .setStatus(toProto(a.status()))
            .setFullName(a.fullName() == null ? "" : a.fullName())
            .setNationalId(a.nationalId() == null ? "" : a.nationalId())
            .setAddress(a.address() == null ? "" : a.address())
            .setReviewerUserId(a.reviewerUserId() != null ? a.reviewerUserId().toString() : "")
            .setRejectionReason(a.rejectionReason() != null ? a.rejectionReason() : "")
            .setCreatedAtEpochMs(a.createdAt() != null ? a.createdAt().toEpochMilli() : 0L)
            .setUpdatedAtEpochMs(a.updatedAt() != null ? a.updatedAt().toEpochMilli() : 0L)
            .build();
}

    private com.banking.kyc.v1.KycDocument toProto(KycDocument d) {
        return com.banking.kyc.v1.KycDocument.newBuilder()
                .setId(d.id().toString())
                .setApplicationId(d.applicationId().toString())
                .setUserId(d.userId().toString())
                .setType(toProto(d.type()))
                .setObjectKey(d.objectKey() == null ? "" : d.objectKey())
                .setOriginalFilename(d.originalFilename() == null ? "" : d.originalFilename())
                .setContentType(d.contentType() == null ? "" : d.contentType())
                .setSizeBytes(d.sizeBytes())
                .setSha256(d.sha256() == null ? "" : d.sha256())
                .setUploadedAtEpochMs(d.uploadedAt() != null ? d.uploadedAt().toEpochMilli() : 0L)
                .build();
    }

    private com.banking.kyc.v1.KycStatus toProto(com.banking.kyc_service.domain.model.KycStatus s) {
        return switch (s) {
            case NOT_SUBMITTED -> com.banking.kyc.v1.KycStatus.KYC_STATUS_NOT_SUBMITTED;
            case PENDING -> com.banking.kyc.v1.KycStatus.KYC_STATUS_PENDING;
            case APPROVED -> com.banking.kyc.v1.KycStatus.KYC_STATUS_APPROVED;
            case REJECTED -> com.banking.kyc.v1.KycStatus.KYC_STATUS_REJECTED;
        };
    }

    private com.banking.kyc.v1.DocumentType toProto(com.banking.kyc_service.domain.model.DocumentType t) {
        return switch (t) {
            case ID_FRONT -> com.banking.kyc.v1.DocumentType.DOCUMENT_TYPE_ID_FRONT;
            case ID_BACK -> com.banking.kyc.v1.DocumentType.DOCUMENT_TYPE_ID_BACK;
            case PROOF_ADDRESS -> com.banking.kyc.v1.DocumentType.DOCUMENT_TYPE_PROOF_ADDRESS;
            case SELFIE -> com.banking.kyc.v1.DocumentType.DOCUMENT_TYPE_SELFIE;
        };
    }
}