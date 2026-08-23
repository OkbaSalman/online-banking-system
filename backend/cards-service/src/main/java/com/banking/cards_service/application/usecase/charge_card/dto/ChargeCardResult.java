package com.banking.cards_service.application.usecase.charge_card.dto;

import com.banking.cards_service.domain.model.CardCharge;

public record ChargeCardResult(
        CardCharge charge
) {}
