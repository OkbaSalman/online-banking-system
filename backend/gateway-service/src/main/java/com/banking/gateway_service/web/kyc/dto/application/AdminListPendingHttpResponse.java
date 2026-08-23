package com.banking.gateway_service.web.kyc.dto.application;

import java.util.List;

public record AdminListPendingHttpResponse(List<KycApplicationHttpDto> applications) {}
