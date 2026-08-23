import { accountTypeLabel, ibanTail } from './format';

/** Prefer the server-persisted display name; fall back to type + IBAN. */
export function describeAccount(account) {
  if (!account) return 'Unknown account';
  const name = String(account.displayName || '').trim();
  return name || `${accountTypeLabel(account.accountType)} ••${ibanTail(account.iban)}`;
}
