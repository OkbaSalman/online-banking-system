package com.banking.transfers_service.application.usecase.get_transfer.dto;

import com.banking.transfers_service.domain.model.Transfer;

public record GetTransferResult(Transfer transfer) {}
