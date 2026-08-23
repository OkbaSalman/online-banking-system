package com.banking.accounts_service.application.port;

import com.banking.accounts_service.domain.model.Account;
import com.banking.accounts_service.domain.model.AccountInvitation;

public interface AccountInvitationNotificationPort {
    void sendInvitationRequested(AccountInvitation invitation, Account account);
}
