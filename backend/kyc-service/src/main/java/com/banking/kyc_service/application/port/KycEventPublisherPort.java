package com.banking.kyc_service.application.port;

import com.banking.kyc_service.application.port.dto.KycStatusChangedEvent;

public interface KycEventPublisherPort {
    void publish(KycStatusChangedEvent event);
}