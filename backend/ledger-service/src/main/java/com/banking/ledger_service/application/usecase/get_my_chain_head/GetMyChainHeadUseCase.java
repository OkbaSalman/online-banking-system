package com.banking.ledger_service.application.usecase.get_my_chain_head;

import com.banking.ledger_service.application.usecase.get_my_chain_head.dto.GetMyChainHeadQuery;
import com.banking.ledger_service.application.usecase.get_my_chain_head.dto.GetMyChainHeadResult;

public interface GetMyChainHeadUseCase {
    GetMyChainHeadResult get(GetMyChainHeadQuery query);
}