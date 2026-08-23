package com.banking.gateway_service.web.accounts.dto.list;

import java.util.List;

import com.banking.gateway_service.web.accounts.dto.account.AccountHttpDto;

public record ListAccountsByTypeHttpResponse(List<AccountHttpDto> accounts) {}
