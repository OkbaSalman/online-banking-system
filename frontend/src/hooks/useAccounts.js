import { useCallback, useEffect, useState } from 'react';
import * as accountsService from '../services/accountsService';
import { getBalances } from '../services/ledgerService';
import { describeAccount } from '../lib/accountNames';

export { describeAccount };

/**
 * Loads the caller's accounts and joins each one with its ledger balance,
 * which lives in a separate service.
 */
export function useAccounts() {
  const [accounts, setAccounts] = useState([]);
  const [balances, setBalances] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const reload = useCallback(async () => {
    setError(null);
    try {
      const list = await accountsService.listMyAccounts();
      setAccounts(list);
      setBalances(list.length ? await getBalances(list.map((account) => account.id)) : {});
      return list;
    } catch (err) {
      setError(err.message);
      return [];
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  const totalCents = accounts.reduce((sum, account) => sum + (balances[account.id] || 0), 0);

  return { accounts, balances, totalCents, loading, error, reload };
}
