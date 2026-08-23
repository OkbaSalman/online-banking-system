package com.banking.kyc_service.application.usecase.kyc.submitKyc.dto;


import java.util.List;
import java.util.UUID;

public record SubmitKycCommand(
        UUID userId,
        String fullName,
        String nationalId,
        String address,
        List<UUID> documentIds
) {}