package com.banking.accounts_service.application.usecase.decline_invitation;

import com.banking.accounts_service.application.usecase.decline_invitation.dto.DeclineInvitationCommand;
import com.banking.accounts_service.application.usecase.decline_invitation.dto.DeclineInvitationResult;

public interface DeclineInvitationUseCase {
    DeclineInvitationResult decline(DeclineInvitationCommand command);
}
