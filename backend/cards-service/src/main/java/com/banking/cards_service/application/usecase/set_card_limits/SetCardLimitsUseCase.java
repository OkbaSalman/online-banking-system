package com.banking.cards_service.application.usecase.set_card_limits;

import com.banking.cards_service.application.usecase.set_card_limits.dto.SetCardLimitsCommand;
import com.banking.cards_service.application.usecase.set_card_limits.dto.SetCardLimitsResult;

public interface SetCardLimitsUseCase {
    SetCardLimitsResult setLimits(SetCardLimitsCommand command);
}
