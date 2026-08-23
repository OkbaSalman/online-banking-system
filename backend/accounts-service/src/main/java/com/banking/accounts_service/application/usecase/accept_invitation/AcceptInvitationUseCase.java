package com.banking.accounts_service.application.usecase.accept_invitation;

import com.banking.accounts_service.application.usecase.accept_invitation.dto.AcceptInvitationCommand;
import com.banking.accounts_service.application.usecase.accept_invitation.dto.AcceptInvitationResult;

public interface AcceptInvitationUseCase {
    AcceptInvitationResult accept(AcceptInvitationCommand command);
}
