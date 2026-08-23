package com.banking.cards_service.application.usecase.create_virtual_card;

import com.banking.cards_service.application.usecase.create_virtual_card.dto.CreateVirtualCardCommand;
import com.banking.cards_service.application.usecase.create_virtual_card.dto.CreateVirtualCardResult;

public interface CreateVirtualCardUseCase {
    CreateVirtualCardResult create(CreateVirtualCardCommand command);
}
