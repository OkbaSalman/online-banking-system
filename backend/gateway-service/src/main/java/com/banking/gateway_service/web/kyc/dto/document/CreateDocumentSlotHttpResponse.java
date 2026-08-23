package com.banking.gateway_service.web.kyc.dto.document;

public record CreateDocumentSlotHttpResponse(
        KycDocumentHttpDto document,
        String uploadUrl
) {}
