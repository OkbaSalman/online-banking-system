package com.banking.gateway_service.web.kyc.dto.submit;

import java.util.List;

public record SubmitKycHttpRequest(
        String fullName,
        String nationalId,
        String address,
        List<String> documentIds
) {}
