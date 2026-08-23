package com.banking.ledger_service.application.usecase.get_my_chain_head;

import com.banking.ledger_service.application.port.LedgerRepositoryPort;
import com.banking.ledger_service.application.usecase.get_my_chain_head.dto.GetMyChainHeadQuery;
import com.banking.ledger_service.application.usecase.get_my_chain_head.dto.GetMyChainHeadResult;

public class GetMyChainHeadService implements GetMyChainHeadUseCase {

    private final LedgerRepositoryPort ledger;

    public GetMyChainHeadService(LedgerRepositoryPort ledger) {
        this.ledger = ledger;
    }

    @Override
    public GetMyChainHeadResult get(GetMyChainHeadQuery query) {
        var head = ledger.getChainHead(query.accountId());
        return new GetMyChainHeadResult(head.headSeq(), head.headHash(), head.headEntryId());
    }
}