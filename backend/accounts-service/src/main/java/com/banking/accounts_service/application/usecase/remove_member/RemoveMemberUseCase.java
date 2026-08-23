package com.banking.accounts_service.application.usecase.remove_member;

import com.banking.accounts_service.application.usecase.remove_member.dto.RemoveMemberCommand;
import com.banking.accounts_service.application.usecase.remove_member.dto.RemoveMemberResult;

public interface RemoveMemberUseCase {
    RemoveMemberResult remove(RemoveMemberCommand command);
}
