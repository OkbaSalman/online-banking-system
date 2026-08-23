package com.banking.accounts_service.application.usecase.is_member;

import com.banking.accounts_service.application.usecase.is_member.dto.IsMemberQuery;
import com.banking.accounts_service.application.usecase.is_member.dto.IsMemberResult;

public interface IsMemberUseCase {
    IsMemberResult isMember(IsMemberQuery query);
}
