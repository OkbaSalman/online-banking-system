package com.banking.gateway_service.web.ledger.dto.list;

import java.util.List;

import com.banking.gateway_service.web.ledger.dto.common.AccountLedgerItemHttpDto;

public record ListAccountEntriesHttpResponse(List<AccountLedgerItemHttpDto> items) {}
