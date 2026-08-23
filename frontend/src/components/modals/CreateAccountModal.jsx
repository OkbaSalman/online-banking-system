import React, { useState } from 'react';
import { PiggyBank, Wallet } from 'lucide-react';
import { Button, ErrorNotice, Label, Modal, TextField } from '../ui';
import { createAccount } from '../../services/accountsService';
import { useToast } from '../../context/ToastContext';
import { SAVINGS_MAX_DEBITS_PER_MONTH } from '../../config';

const ACCOUNT_TYPES = [
  {
    value: 'CHECKING',
    label: 'Checking',
    icon: Wallet,
    blurb: 'Everyday spending with no limit on outgoing transfers.',
  },
  {
    value: 'SAVINGS',
    label: 'Savings',
    icon: PiggyBank,
    blurb: `Earns monthly interest. Limited to ${SAVINGS_MAX_DEBITS_PER_MONTH} outgoing transfers per month.`,
  },
];

export default function CreateAccountModal({ open, onClose, onCreated }) {
  const toast = useToast();
  const [accountType, setAccountType] = useState('CHECKING');
  const [nickname, setNickname] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const reset = () => {
    setAccountType('CHECKING');
    setNickname('');
    setError(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      const account = await createAccount(accountType, nickname.trim());
      toast.success(
        'Account opened',
        `${account.displayName || account.iban} is ready to use.`,
      );
      reset();
      onClose();
      await onCreated?.(account);
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
      title="Open a new account"
      description="Pick the account type. An IBAN is generated for you straight away."
    >
      <form onSubmit={handleSubmit} className="space-y-5">
        <ErrorNotice>{error}</ErrorNotice>

        <div>
          <Label>Account type</Label>
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            {ACCOUNT_TYPES.map(({ value, label, icon: Icon, blurb }) => {
              const selected = accountType === value;
              return (
                <button
                  key={value}
                  type="button"
                  onClick={() => setAccountType(value)}
                  className={`cursor-pointer rounded-2xl border p-4 text-left transition-all ${
                    selected
                      ? 'border-[#3525cd] bg-[#eff4ff] shadow-[0_10px_25px_rgba(53,37,205,0.12)]'
                      : 'border-slate-200 bg-white hover:border-slate-300'
                  }`}
                >
                  <Icon size={18} className={selected ? 'text-[#3525cd]' : 'text-slate-400'} />
                  <p className="mt-2 text-sm font-bold text-[#0b1c30]">{label}</p>
                  <p className="mt-1 text-[11px] leading-relaxed text-[#777587]">{blurb}</p>
                </button>
              );
            })}
          </div>
        </div>

        <TextField
          label="Account name"
          value={nickname}
          onChange={(event) => setNickname(event.target.value)}
          placeholder="Rent account"
          hint="Saved on the account so you can tell them apart. Leave blank to use the IBAN."
          maxLength={80}
        />

        <Button type="submit" size="lg" loading={submitting}>
          Open account
        </Button>
      </form>
    </Modal>
  );
}
