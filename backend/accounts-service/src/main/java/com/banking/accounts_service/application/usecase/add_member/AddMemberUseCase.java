package com.banking.accounts_service.application.usecase.add_member;

import com.banking.accounts_service.application.usecase.add_member.dto.AddMemberCommand;
import com.banking.accounts_service.application.usecase.add_member.dto.AddMemberResult;

public interface AddMemberUseCase {
    AddMemberResult add(AddMemberCommand command);
}
