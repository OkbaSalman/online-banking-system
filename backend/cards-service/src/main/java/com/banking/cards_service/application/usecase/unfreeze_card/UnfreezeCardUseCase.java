package com.banking.cards_service.application.usecase.unfreeze_card;

import com.banking.cards_service.application.usecase.unfreeze_card.dto.UnfreezeCardCommand;
import com.banking.cards_service.application.usecase.unfreeze_card.dto.UnfreezeCardResult;

public interface UnfreezeCardUseCase {
    UnfreezeCardResult unfreeze(UnfreezeCardCommand command);
}
