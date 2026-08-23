package com.banking.ledger_service.application.usecase.verify_my_chain;

import com.banking.ledger_service.application.port.LedgerRepositoryPort;
import com.banking.ledger_service.application.usecase.verify_my_chain.dto.VerifyMyChainQuery;
import com.banking.ledger_service.application.usecase.verify_my_chain.dto.VerifyMyChainResult;

public class VerifyMyChainService implements VerifyMyChainUseCase {

    private final LedgerRepositoryPort ledger;

    public VerifyMyChainService(LedgerRepositoryPort ledger) {
        this.ledger = ledger;
    }

    @Override
    public VerifyMyChainResult verify(VerifyMyChainQuery query) {
        var scan = ledger.verifyChain(query.accountId());
        return new VerifyMyChainResult(scan.ok(), scan.firstInvalidSeq(), scan.message());
    }
}