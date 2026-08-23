package com.banking.cards_service.application.usecase.get_card;

import com.banking.cards_service.application.usecase.get_card.dto.GetCardQuery;
import com.banking.cards_service.application.usecase.get_card.dto.GetCardResult;

public interface GetCardUseCase {
    GetCardResult get(GetCardQuery query);
}
