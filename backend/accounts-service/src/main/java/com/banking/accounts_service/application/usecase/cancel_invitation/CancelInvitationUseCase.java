package com.banking.accounts_service.application.usecase.cancel_invitation;

import com.banking.accounts_service.application.usecase.cancel_invitation.dto.CancelInvitationCommand;
import com.banking.accounts_service.application.usecase.cancel_invitation.dto.CancelInvitationResult;

public interface CancelInvitationUseCase {
    CancelInvitationResult cancel(CancelInvitationCommand command);
}
