package com.banking.cards_service.application.usecase.list_my_charges;

import com.banking.cards_service.application.usecase.list_my_charges.dto.ListMyChargesQuery;
import com.banking.cards_service.application.usecase.list_my_charges.dto.ListMyChargesResult;

public interface ListMyChargesUseCase {
    ListMyChargesResult list(ListMyChargesQuery query);
}
