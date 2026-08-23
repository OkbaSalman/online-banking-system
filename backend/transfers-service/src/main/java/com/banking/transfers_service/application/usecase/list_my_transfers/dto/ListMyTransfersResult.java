package com.banking.transfers_service.application.usecase.list_my_transfers.dto;

import com.banking.transfers_service.domain.model.Transfer;

import java.util.List;

public record ListMyTransfersResult(List<Transfer> transfers) {}
