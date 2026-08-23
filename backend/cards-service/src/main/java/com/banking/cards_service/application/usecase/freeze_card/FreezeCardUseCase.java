package com.banking.cards_service.application.usecase.freeze_card;

import com.banking.cards_service.application.usecase.freeze_card.dto.FreezeCardCommand;
import com.banking.cards_service.application.usecase.freeze_card.dto.FreezeCardResult;

public interface FreezeCardUseCase {
    FreezeCardResult freeze(FreezeCardCommand command);
}
