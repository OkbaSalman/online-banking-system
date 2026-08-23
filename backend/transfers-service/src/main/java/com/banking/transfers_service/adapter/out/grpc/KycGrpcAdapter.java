package com.banking.transfers_service.adapter.out.grpc;

import com.banking.kyc.v1.GetMyKycRequest;
import com.banking.kyc.v1.KycServiceGrpc;
import com.banking.transfers_service.application.port.KycClientPort;
import com.banking.transfers_service.application.port.KycStatus;

public class KycGrpcAdapter implements KycClientPort {

    private final KycServiceGrpc.KycServiceBlockingStub kyc;

    public KycGrpcAdapter(KycServiceGrpc.KycServiceBlockingStub kyc) {
        this.kyc = kyc;
    }

    @Override
    public KycStatus getMyKycStatus() {
        var res = kyc.getMyKyc(GetMyKycRequest.newBuilder().build());
        if (res == null || !res.hasApplication()) {
            return KycStatus.NOT_SUBMITTED;
        }

        return switch (res.getApplication().getStatus()) {
            case KYC_STATUS_APPROVED -> KycStatus.APPROVED;
            case KYC_STATUS_PENDING -> KycStatus.PENDING;
            case KYC_STATUS_REJECTED -> KycStatus.REJECTED;
            case KYC_STATUS_NOT_SUBMITTED, KYC_STATUS_UNSPECIFIED, UNRECOGNIZED -> KycStatus.NOT_SUBMITTED;
        };
    }
}
