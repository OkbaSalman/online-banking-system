package com.banking.cards_service.application.usecase.charge_card;

import com.banking.cards_service.application.usecase.charge_card.dto.ChargeCardCommand;
import com.banking.cards_service.application.usecase.charge_card.dto.ChargeCardResult;

public interface ChargeCardUseCase {
    ChargeCardResult charge(ChargeCardCommand command);
}
