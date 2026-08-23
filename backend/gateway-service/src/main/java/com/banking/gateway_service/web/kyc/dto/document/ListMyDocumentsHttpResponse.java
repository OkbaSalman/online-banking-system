package com.banking.gateway_service.web.kyc.dto.document;

import java.util.List;

public record ListMyDocumentsHttpResponse(List<KycDocumentHttpDto> documents) {}
