package com.banking.accounts_service.config;

import com.banking.accounts_service.adapter.out.jpa.AccountsJpaAdapter;
import com.banking.accounts_service.adapter.out.jpa.AccountInvitationsJpaAdapter;
import com.banking.accounts_service.adapter.out.jpa.repository.AccountJpaRepository;
import com.banking.accounts_service.adapter.out.jpa.repository.AccountInvitationJpaRepository;
import com.banking.accounts_service.adapter.out.jpa.repository.AccountMembershipJpaRepository;
import com.banking.accounts_service.application.port.AccountInvitationNotificationPort;
import com.banking.accounts_service.application.port.AccountInvitationsRepositoryPort;
import com.banking.accounts_service.application.port.AccountsRepositoryPort;
import com.banking.accounts_service.application.usecase.accept_invitation.AcceptInvitationService;
import com.banking.accounts_service.application.usecase.accept_invitation.AcceptInvitationUseCase;
import com.banking.accounts_service.application.usecase.add_member.AddMemberService;
import com.banking.accounts_service.application.usecase.add_member.AddMemberUseCase;
import com.banking.accounts_service.application.usecase.can_debit.CanDebitService;
import com.banking.accounts_service.application.usecase.can_debit.CanDebitUseCase;
import com.banking.accounts_service.application.usecase.create_account.CreateAccountService;
import com.banking.accounts_service.application.usecase.create_account.CreateAccountUseCase;
import com.banking.accounts_service.application.usecase.decline_invitation.DeclineInvitationService;
import com.banking.accounts_service.application.usecase.decline_invitation.DeclineInvitationUseCase;
import com.banking.accounts_service.application.usecase.get_account.GetAccountService;
import com.banking.accounts_service.application.usecase.get_account.GetAccountUseCase;
import com.banking.accounts_service.application.usecase.invite_member.InviteMemberService;
import com.banking.accounts_service.application.usecase.invite_member.InviteMemberUseCase;
import com.banking.accounts_service.application.usecase.is_member.IsMemberService;
import com.banking.accounts_service.application.usecase.is_member.IsMemberUseCase;
import com.banking.accounts_service.application.usecase.list_account_invitations.ListAccountInvitationsService;
import com.banking.accounts_service.application.usecase.list_account_invitations.ListAccountInvitationsUseCase;
import com.banking.accounts_service.application.usecase.list_account_members.ListAccountMembersService;
import com.banking.accounts_service.application.usecase.list_account_members.ListAccountMembersUseCase;
import com.banking.accounts_service.application.usecase.list_accounts_by_type.ListAccountsByTypeService;
import com.banking.accounts_service.application.usecase.list_accounts_by_type.ListAccountsByTypeUseCase;
import com.banking.accounts_service.application.usecase.list_my_accounts.ListMyAccountsService;
import com.banking.accounts_service.application.usecase.list_my_accounts.ListMyAccountsUseCase;
import com.banking.accounts_service.application.usecase.list_my_invitations.ListMyInvitationsService;
import com.banking.accounts_service.application.usecase.list_my_invitations.ListMyInvitationsUseCase;
import com.banking.accounts_service.application.usecase.cancel_invitation.CancelInvitationService;
import com.banking.accounts_service.application.usecase.cancel_invitation.CancelInvitationUseCase;
import com.banking.accounts_service.application.usecase.remove_member.RemoveMemberService;
import com.banking.accounts_service.application.usecase.remove_member.RemoveMemberUseCase;
import com.banking.accounts_service.application.usecase.set_account_display_name.SetAccountDisplayNameService;
import com.banking.accounts_service.application.usecase.set_account_display_name.SetAccountDisplayNameUseCase;
import com.banking.accounts_service.application.usecase.set_account_frozen.SetAccountFrozenService;
import com.banking.accounts_service.application.usecase.set_account_frozen.SetAccountFrozenUseCase;
import com.banking.accounts_service.domain.service.IbanGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccountsUseCaseConfig {

    @Bean
    AccountsRepositoryPort accountsRepositoryPort(AccountJpaRepository accounts, AccountMembershipJpaRepository memberships) {
        return new AccountsJpaAdapter(accounts, memberships);
    }

    @Bean
    AccountInvitationsRepositoryPort accountInvitationsRepositoryPort(AccountInvitationJpaRepository invitations) {
        return new AccountInvitationsJpaAdapter(invitations);
    }

    @Bean
    IbanGenerator ibanGenerator(
            @Value("${accounts.iban.country-code:DE}") String countryCode,
            @Value("${accounts.iban.bank-code:BANK}") String bankCode
    ) {
        return new IbanGenerator(countryCode, bankCode);
    }

    @Bean
    CreateAccountUseCase createAccountUseCase(AccountsRepositoryPort accounts, IbanGenerator ibanGenerator) {
        return new CreateAccountService(accounts, ibanGenerator);
    }

    @Bean
    GetAccountUseCase getAccountUseCase(AccountsRepositoryPort accounts) {
        return new GetAccountService(accounts);
    }

    @Bean
    ListMyAccountsUseCase listMyAccountsUseCase(AccountsRepositoryPort accounts) {
        return new ListMyAccountsService(accounts);
    }

    @Bean
    ListAccountsByTypeUseCase listAccountsByTypeUseCase(AccountsRepositoryPort accounts) {
        return new ListAccountsByTypeService(accounts);
    }

    @Bean
    AddMemberUseCase addMemberUseCase(AccountsRepositoryPort accounts) {
        return new AddMemberService(accounts);
    }

    @Bean
    IsMemberUseCase isMemberUseCase(AccountsRepositoryPort accounts) {
        return new IsMemberService(accounts);
    }

    @Bean
    CanDebitUseCase canDebitUseCase(AccountsRepositoryPort accounts) {
        return new CanDebitService(accounts);
    }

    @Bean
    SetAccountFrozenUseCase setAccountFrozenUseCase(AccountsRepositoryPort accounts) {
        return new SetAccountFrozenService(accounts);
    }

    @Bean
    SetAccountDisplayNameUseCase setAccountDisplayNameUseCase(AccountsRepositoryPort accounts) {
        return new SetAccountDisplayNameService(accounts);
    }

    @Bean
    InviteMemberUseCase inviteMemberUseCase(
            AccountsRepositoryPort accounts,
            AccountInvitationsRepositoryPort invitations,
            AccountInvitationNotificationPort notifications
    ) {
        return new InviteMemberService(accounts, invitations, notifications);
    }

    @Bean
    ListMyInvitationsUseCase listMyInvitationsUseCase(AccountInvitationsRepositoryPort invitations) {
        return new ListMyInvitationsService(invitations);
    }

    @Bean
    ListAccountInvitationsUseCase listAccountInvitationsUseCase(
            AccountsRepositoryPort accounts,
            AccountInvitationsRepositoryPort invitations
    ) {
        return new ListAccountInvitationsService(accounts, invitations);
    }

    @Bean
    AcceptInvitationUseCase acceptInvitationUseCase(AccountsRepositoryPort accounts, AccountInvitationsRepositoryPort invitations) {
        return new AcceptInvitationService(accounts, invitations);
    }

    @Bean
    DeclineInvitationUseCase declineInvitationUseCase(AccountInvitationsRepositoryPort invitations) {
        return new DeclineInvitationService(invitations);
    }

    @Bean
    CancelInvitationUseCase cancelInvitationUseCase(AccountsRepositoryPort accounts, AccountInvitationsRepositoryPort invitations) {
        return new CancelInvitationService(accounts, invitations);
    }

    @Bean
    ListAccountMembersUseCase listAccountMembersUseCase(AccountsRepositoryPort accounts) {
        return new ListAccountMembersService(accounts);
    }

    @Bean
    RemoveMemberUseCase removeMemberUseCase(AccountsRepositoryPort accounts) {
        return new RemoveMemberService(accounts);
    }
}
