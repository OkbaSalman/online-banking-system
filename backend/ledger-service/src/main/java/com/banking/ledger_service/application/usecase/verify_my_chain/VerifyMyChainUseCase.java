package com.banking.ledger_service.application.usecase.verify_my_chain;

import com.banking.ledger_service.application.usecase.verify_my_chain.dto.VerifyMyChainQuery;
import com.banking.ledger_service.application.usecase.verify_my_chain.dto.VerifyMyChainResult;

public interface VerifyMyChainUseCase {
    VerifyMyChainResult verify(VerifyMyChainQuery query);
}