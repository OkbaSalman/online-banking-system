package com.banking.accounts_service.application.usecase.invite_member;

import com.banking.accounts_service.application.usecase.invite_member.dto.InviteMemberCommand;
import com.banking.accounts_service.application.usecase.invite_member.dto.InviteMemberResult;

public interface InviteMemberUseCase {
    InviteMemberResult invite(InviteMemberCommand command);
}
