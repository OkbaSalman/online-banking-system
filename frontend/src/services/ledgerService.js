import { api, unwrap } from '../api/client';

export const getBalance = (accountId) => unwrap(api.get(`/ledger/balance/${accountId}`));

export const getEntry = (entryId) =>
  unwrap(api.get(`/ledger/entries/${entryId}`)).then((data) => data.entry);

export const listAccountEntries = (accountId, { limit = 50, offset = 0 } = {}) =>
  unwrap(api.get(`/ledger/accounts/${accountId}/entries`, { params: { limit, offset } })).then(
    (data) => data.items || [],
  );

export const getChainHead = (accountId) => unwrap(api.get(`/ledger/accounts/${accountId}/chain-head`));

/** Re-hashes the account's item chain server-side and reports the first break. */
export const verifyChain = (accountId) =>
  unwrap(api.get(`/ledger/accounts/${accountId}/verify-chain`));

/** Resolves balances for many accounts at once, tolerating per-account failures. */
export async function getBalances(accountIds) {
  const results = await Promise.allSettled(accountIds.map((id) => getBalance(id)));
  const balances = {};
  results.forEach((result, index) => {
    balances[accountIds[index]] =
      result.status === 'fulfilled' ? Number(result.value.availableCents) || 0 : null;
  });
  return balances;
}
