import React from 'react';
import { SelectField } from './ui';
import { describeAccount } from '../hooks/useAccounts';
import { formatMoney } from '../lib/format';

export default function AccountSelect({
  accounts,
  balances = {},
  label = 'Account',
  hint,
  placeholder = 'Select an account',
  ...props
}) {
  return (
    <SelectField label={label} hint={hint} {...props}>
      <option value="">{placeholder}</option>
      {accounts.map((account) => {
        const balance = balances[account.id];
        const suffix = balance === null || balance === undefined ? '' : ` — ${formatMoney(balance)}`;
        return (
          <option key={account.id} value={account.id}>
            {describeAccount(account)}
            {suffix}
            {account.frozen ? ' (frozen)' : ''}
          </option>
        );
      })}
    </SelectField>
  );
}
