package com.banking.kyc_service.application.port;

import java.util.UUID;

public interface EmailNotificationPort {
    void sendKycStatusChanged(UUID userId, String status, String rejectionReason);
}
