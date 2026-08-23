import React, { useMemo, useState } from 'react';
import { ArrowRightLeft } from 'lucide-react';
import { Button, ErrorNotice, Modal, MoneyField, TextField } from '../ui';
import AccountSelect from '../AccountSelect';
import { createTransfer } from '../../services/transfersService';
import { useToast } from '../../context/ToastContext';
import { TRANSFER_FEE_BPS } from '../../config';
import { calculateFeeCents, dollarsToCents, formatMoney, isSavings, isUuid } from '../../lib/format';

export default function TransferModal({ open, onClose, accounts, balances, onCompleted }) {
  const toast = useToast();

  const [fromAccountId, setFromAccountId] = useState('');
  const [toAccountId, setToAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const amountCents = dollarsToCents(amount);
  const feeCents = calculateFeeCents(amountCents || 0);
  const totalCents = (amountCents || 0) + feeCents;

  const fromAccount = accounts.find((account) => account.id === fromAccountId);
  const availableCents = balances?.[fromAccountId];

  const ownAccountOptions = useMemo(
    () => accounts.filter((account) => account.id !== fromAccountId),
    [accounts, fromAccountId],
  );

  const reset = () => {
    setAmount('');
    setDescription('');
    setToAccountId('');
    setError(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);

    if (!fromAccountId) return setError('Choose the account the money leaves from.');
    if (!isUuid(toAccountId)) {
      return setError('The destination must be an account id (UUID). Copy it from the Accounts page or ask the recipient for it.');
    }
    if (fromAccountId === toAccountId) return setError('Pick two different accounts.');
    if (!amountCents) return setError('Enter an amount greater than zero.');
    if (availableCents !== null && availableCents !== undefined && totalCents > availableCents) {
      return setError(
        `Not enough available balance. This transfer needs ${formatMoney(totalCents)} including the fee.`,
      );
    }

    setSubmitting(true);
    try {
      const result = await createTransfer({
        fromAccountId,
        toAccountId: toAccountId.trim(),
        amountCents,
        description: description.trim(),
      });
      toast.success(
        'Transfer sent',
        `${formatMoney(result.transfer.amountCents)} moved. Fee ${formatMoney(result.transfer.feeCents)}. New balance ${formatMoney(result.fromBalanceCents)}.`,
      );
      reset();
      onClose();
      await onCompleted?.(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Send a transfer"
      description="Money moves through the double-entry ledger. The sender pays the transfer fee on top of the amount."
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <ErrorNotice>{error}</ErrorNotice>

        <AccountSelect
          label="From"
          accounts={accounts}
          balances={balances}
          value={fromAccountId}
          onChange={(event) => setFromAccountId(event.target.value)}
          placeholder="Select the source account"
          required
        />

        {fromAccount && isSavings(fromAccount) && (
          <p className="rounded-xl bg-amber-50 px-3 py-2 text-[11px] font-medium text-amber-800">
            Savings accounts have a capped number of outgoing transfers each month.
          </p>
        )}

        <TextField
          label="To (account id)"
          value={toAccountId}
          onChange={(event) => setToAccountId(event.target.value)}
          placeholder="00000000-0000-0000-0000-000000000000"
          className="font-mono text-xs"
          hint="Transfers are routed by account id."
          required
        />

        {ownAccountOptions.length > 0 && (
          <div className="flex flex-wrap gap-2">
            <span className="text-[10px] font-bold uppercase tracking-wider text-[#777587]">
              Your accounts:
            </span>
            {ownAccountOptions.map((account) => (
              <button
                key={account.id}
                type="button"
                onClick={() => setToAccountId(account.id)}
                className="cursor-pointer rounded-lg bg-[#eff4ff] px-2 py-1 text-[10px] font-bold text-[#3525cd] transition-colors hover:bg-[#dfe7ff]"
              >
                ••{account.iban?.slice(-4)}
              </button>
            ))}
          </div>
        )}

        <MoneyField
          label="Amount"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          placeholder="0.00"
          required
        />

        <TextField
          label="Reference (optional)"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          placeholder="Rent for March"
          maxLength={140}
        />

        {amountCents ? (
          <div className="space-y-1.5 rounded-2xl bg-[#eff4ff] p-4 text-xs">
            <div className="flex justify-between text-[#464555]">
              <span>Amount to recipient</span>
              <span className="font-semibold text-[#0b1c30]">{formatMoney(amountCents)}</span>
            </div>
            <div className="flex justify-between text-[#464555]">
              <span>Transfer fee ({(TRANSFER_FEE_BPS / 100).toFixed(2)}%)</span>
              <span className="font-semibold text-[#0b1c30]">{formatMoney(feeCents)}</span>
            </div>
            <div className="mt-2 flex justify-between border-t border-[#c7c4d8]/50 pt-2 text-sm">
              <span className="font-bold text-[#0b1c30]">Total debited</span>
              <span className="font-black text-[#3525cd]">{formatMoney(totalCents)}</span>
            </div>
          </div>
        ) : null}

        <Button type="submit" size="lg" loading={submitting} icon={ArrowRightLeft}>
          Send transfer
        </Button>
      </form>
    </Modal>
  );
}
