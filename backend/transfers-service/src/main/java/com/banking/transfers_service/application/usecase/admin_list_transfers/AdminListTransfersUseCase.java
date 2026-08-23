package com.banking.transfers_service.application.usecase.admin_list_transfers;

import com.banking.transfers_service.application.usecase.admin_list_transfers.dto.AdminListTransfersQuery;
import com.banking.transfers_service.application.usecase.admin_list_transfers.dto.AdminListTransfersResult;

public interface AdminListTransfersUseCase {
    AdminListTransfersResult list(AdminListTransfersQuery query);
}
