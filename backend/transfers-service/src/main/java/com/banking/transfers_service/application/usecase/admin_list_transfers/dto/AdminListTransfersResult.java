package com.banking.transfers_service.application.usecase.admin_list_transfers.dto;

import com.banking.transfers_service.domain.model.Transfer;

import java.util.List;

public record AdminListTransfersResult(List<Transfer> transfers) {}
