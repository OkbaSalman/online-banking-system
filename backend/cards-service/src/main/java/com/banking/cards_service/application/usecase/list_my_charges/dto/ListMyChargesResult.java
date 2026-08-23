package com.banking.cards_service.application.usecase.list_my_charges.dto;

import com.banking.cards_service.domain.model.CardCharge;

import java.util.List;

public record ListMyChargesResult(
        List<CardCharge> charges
) {}
