import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { ArrowRightLeft, Download, Inbox, Info, Send } from 'lucide-react';
import AppLayout from '../components/layout/AppLayout';
import AccountSelect from '../components/AccountSelect';
import {
  Badge,
  Button,
  Card,
  DataRow,
  EmptyState,
  ErrorNotice,
  InfoNotice,
  Modal,
  MoneyField,
  SectionTitle,
  SelectField,
  Spinner,
  StatusBadge,
  TextField,
} from '../components/ui';
import { useAccounts } from '../hooks/useAccounts';
import { useToast } from '../context/ToastContext';
import { createTransfer, listMyTransfers } from '../services/transfersService';
import { TRANSFER_FEE_BPS } from '../config';
import {
  calculateFeeCents,
  dollarsToCents,
  formatDateTime,
  formatMoney,
  isSavings,
  isUuid,
} from '../lib/format';
import { exportReceiptPdf, transferReceiptRows } from '../lib/receiptPdf';

const STATUS_FILTERS = [
  { value: '', label: 'All statuses' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'BLOCKED', label: 'Blocked' },
  { value: 'FAILED', label: 'Failed' },
];

export default function PaymentsPage() {
  const toast = useToast();
  const { accounts, balances, loading: accountsLoading, reload } = useAccounts();

  const [fromAccountId, setFromAccountId] = useState('');
  const [toAccountId, setToAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const [transfers, setTransfers] = useState([]);
  const [transfersLoading, setTransfersLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [accountFilter, setAccountFilter] = useState('');
  const [selectedTransfer, setSelectedTransfer] = useState(null);

  const amountCents = dollarsToCents(amount);
  const feeCents = calculateFeeCents(amountCents || 0);
  const totalCents = (amountCents || 0) + feeCents;
  const fromAccount = accounts.find((account) => account.id === fromAccountId);
  const availableCents = balances[fromAccountId];

  useEffect(() => {
    if (!fromAccountId && accounts.length) setFromAccountId(accounts[0].id);
  }, [accounts, fromAccountId]);

  const loadTransfers = useCallback(async () => {
    setTransfersLoading(true);
    try {
      setTransfers(
        await listMyTransfers({
          status: statusFilter || undefined,
          fromAccountId: accountFilter || undefined,
          limit: 50,
        }),
      );
    } catch (err) {
      toast.error('Could not load transfers', err.message);
    } finally {
      setTransfersLoading(false);
    }
  }, [statusFilter, accountFilter, toast]);

  useEffect(() => {
    loadTransfers();
  }, [loadTransfers]);

  const myAccountIds = useMemo(() => new Set(accounts.map((account) => account.id)), [accounts]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);

    if (!fromAccountId) return setError('Choose the account the money leaves from.');
    if (!isUuid(toAccountId)) {
      return setError('The destination has to be an account id (UUID). Ask the recipient to copy it from their Accounts page.');
    }
    if (fromAccountId === toAccountId.trim()) return setError('Source and destination must differ.');
    if (!amountCents) return setError('Enter an amount greater than zero.');
    if (availableCents !== null && availableCents !== undefined && totalCents > availableCents) {
      return setError(`This transfer needs ${formatMoney(totalCents)} including the fee, more than the account holds.`);
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
        'Transfer completed',
        `${formatMoney(result.transfer.amountCents)} sent. Remaining balance ${formatMoney(result.fromBalanceCents)}.`,
      );
      setAmount('');
      setDescription('');
      setToAccountId('');
      await Promise.all([reload(), loadTransfers()]);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppLayout
      eyebrow="Payments"
      title="Send money"
      description="Transfers settle through the ledger immediately. The sender pays a small percentage fee on top of the amount."
    >
      <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
        <div className="space-y-6 lg:col-span-7">
          <Card className="p-6 sm:p-8">
            <SectionTitle
              icon={Send}
              title="New transfer"
              description="Money is routed by account id, so the recipient does not need to be someone you know."
            />

            {accountsLoading ? (
              <Spinner label="Loading your accounts…" />
            ) : accounts.length === 0 ? (
              <EmptyState
                icon={Inbox}
                title="No account to send from"
                description="Open an account first from the Accounts page."
              />
            ) : (
              <form onSubmit={handleSubmit} className="space-y-4">
                <ErrorNotice>{error}</ErrorNotice>

                <AccountSelect
                  label="From"
                  accounts={accounts}
                  balances={balances}
                  value={fromAccountId}
                  onChange={(event) => setFromAccountId(event.target.value)}
                  required
                />

                {fromAccount && isSavings(fromAccount) && (
                  <p className="rounded-xl bg-amber-50 px-3 py-2 text-[11px] font-medium text-amber-800">
                    Savings accounts allow a limited number of outgoing transfers each month.
                  </p>
                )}

                {fromAccount?.frozen && (
                  <p className="rounded-xl bg-rose-50 px-3 py-2 text-[11px] font-medium text-rose-800">
                    This account is frozen. An administrator has to unfreeze it before money can leave.
                  </p>
                )}

                <TextField
                  label="Recipient account id"
                  value={toAccountId}
                  onChange={(event) => setToAccountId(event.target.value)}
                  placeholder="00000000-0000-0000-0000-000000000000"
                  className="font-mono text-xs"
                  required
                />

                {accounts.filter((account) => account.id !== fromAccountId).length > 0 && (
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-[#777587]">
                      Move between your accounts:
                    </span>
                    {accounts
                      .filter((account) => account.id !== fromAccountId)
                      .map((account) => (
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
                  placeholder="Invoice 1042"
                  maxLength={140}
                />

                {amountCents ? (
                  <div className="space-y-1.5 rounded-2xl bg-[#eff4ff] p-4 text-xs">
                    <div className="flex justify-between text-[#464555]">
                      <span>Amount to recipient</span>
                      <span className="font-semibold text-[#0b1c30]">{formatMoney(amountCents)}</span>
                    </div>
                    <div className="flex justify-between text-[#464555]">
                      <span>Fee ({(TRANSFER_FEE_BPS / 100).toFixed(2)}%)</span>
                      <span className="font-semibold text-[#0b1c30]">{formatMoney(feeCents)}</span>
                    </div>
                    <div className="mt-2 flex justify-between border-t border-[#c7c4d8]/50 pt-2 text-sm">
                      <span className="font-bold text-[#0b1c30]">Total debited</span>
                      <span className="font-black text-[#3525cd]">{formatMoney(totalCents)}</span>
                    </div>
                    {availableCents !== null && availableCents !== undefined && (
                      <p className="pt-1 text-[11px] text-[#777587]">
                        Balance after this transfer: {formatMoney(availableCents - totalCents)}
                      </p>
                    )}
                  </div>
                ) : null}

                <Button type="submit" size="lg" loading={submitting} icon={ArrowRightLeft}>
                  Send transfer
                </Button>
              </form>
            )}
          </Card>
        </div>

        <div className="space-y-6 lg:col-span-5">
          <Card className="p-6">
            <SectionTitle
              icon={Info}
              title="How to send"
              description="Transfers settle against an account id, not an email address."
            />
            <InfoNotice icon={Info} title="Ask for their account id">
              The recipient copies the full UUID from their Accounts page. Searching other customers
              by email is not available — that keeps the directory private.
            </InfoNotice>
          </Card>
        </div>
      </div>

      <Card className="mt-8 p-6 sm:p-8">
        <SectionTitle
          icon={ArrowRightLeft}
          title="Transfer history"
          description="Everything you initiated, newest first."
          action={
            <div className="flex flex-wrap gap-2">
              <SelectField
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value)}
                className="!py-2.5 text-xs"
              >
                {STATUS_FILTERS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </SelectField>
              <SelectField
                value={accountFilter}
                onChange={(event) => setAccountFilter(event.target.value)}
                className="!py-2.5 text-xs"
              >
                <option value="">All source accounts</option>
                {accounts.map((account) => (
                  <option key={account.id} value={account.id}>
                    ••{account.iban?.slice(-4)}
                  </option>
                ))}
              </SelectField>
            </div>
          }
        />

        {transfersLoading ? (
          <Spinner label="Loading transfers…" />
        ) : transfers.length === 0 ? (
          <EmptyState
            icon={Inbox}
            title="No transfers match"
            description="Change the filters, or send your first transfer using the form above."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[760px] border-collapse text-left">
              <thead>
                <tr className="border-b border-slate-100">
                  {['Date', 'Reference', 'Direction', 'Status', 'Fee', 'Amount', ''].map((heading, index) => (
                    <th
                      key={heading || index}
                      className={`py-3 font-mono text-[10px] font-bold uppercase tracking-widest text-slate-400 ${
                        index >= 4 ? 'text-right' : ''
                      }`}
                    >
                      {heading}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50 text-xs">
                {transfers.map((transfer) => {
                  const fromMine = myAccountIds.has(transfer.fromAccountId);
                  const toMine = myAccountIds.has(transfer.toAccountId);
                  const direction =
                    fromMine && toMine ? 'Between my accounts' : fromMine ? 'Sent' : 'Received';
                  const incoming = direction === 'Received';
                  return (
                    <tr key={transfer.id} className="transition-colors hover:bg-slate-50/70">
                      <td className="whitespace-nowrap py-4 font-mono text-[11px] text-slate-500">
                        {formatDateTime(transfer.createdAtEpochMs)}
                      </td>
                      <td className="max-w-[16rem] py-4 font-bold text-[#0b1c30]">
                        <span className="break-words">{transfer.description || 'Transfer'}</span>
                      </td>
                      <td className="py-4">
                        <Badge
                          tone={
                            direction === 'Received'
                              ? 'emerald'
                              : direction === 'Between my accounts'
                                ? 'indigo'
                                : 'neutral'
                          }
                        >
                          {direction}
                        </Badge>
                      </td>
                      <td className="py-4">
                        <StatusBadge status={transfer.status} />
                      </td>
                      <td className="whitespace-nowrap py-4 text-right font-mono text-[11px] text-slate-400">
                        {transfer.feeCents ? formatMoney(transfer.feeCents) : '—'}
                      </td>
                      <td
                        className={`whitespace-nowrap py-4 text-right font-mono font-black ${
                          incoming ? 'text-emerald-600' : 'text-rose-600'
                        }`}
                      >
                        {incoming ? '+' : '-'}
                        {formatMoney(transfer.amountCents)}
                      </td>
                      <td className="whitespace-nowrap py-4 text-right">
                        <button
                          type="button"
                          onClick={() => setSelectedTransfer(transfer)}
                          className="cursor-pointer rounded-lg bg-[#eff4ff] px-3 py-1.5 text-[10px] font-black uppercase text-[#3525cd] transition-colors hover:bg-[#3525cd] hover:text-white"
                        >
                          Receipt
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <Modal
        open={Boolean(selectedTransfer)}
        onClose={() => setSelectedTransfer(null)}
        title="Transfer receipt"
        description="The record as stored by transfers-service."
      >
        {selectedTransfer && (
          <div className="space-y-4">
          <div className="divide-y divide-slate-100 rounded-2xl border border-slate-100 bg-slate-50 p-4">
            <DataRow label="Status">
              <StatusBadge status={selectedTransfer.status} />
            </DataRow>
            <DataRow label="Amount">{formatMoney(selectedTransfer.amountCents)}</DataRow>
            <DataRow label="Fee">{formatMoney(selectedTransfer.feeCents)}</DataRow>
            <DataRow label="Total debited">
              {formatMoney(selectedTransfer.amountCents + selectedTransfer.feeCents)}
            </DataRow>
            <DataRow label="From">
              <span className="font-mono text-[11px]">{selectedTransfer.fromAccountId}</span>
            </DataRow>
            <DataRow label="To">
              <span className="font-mono text-[11px]">{selectedTransfer.toAccountId}</span>
            </DataRow>
            <DataRow label="Reference">{selectedTransfer.description || '—'}</DataRow>
            <DataRow label="Booked">{formatDateTime(selectedTransfer.createdAtEpochMs)}</DataRow>
            <DataRow label="Ledger entry">
              <span className="font-mono text-[11px]">{selectedTransfer.ledgerEntryId || '—'}</span>
            </DataRow>
            <DataRow label="Fee entry">
              <span className="font-mono text-[11px]">{selectedTransfer.feeLedgerEntryId || '—'}</span>
            </DataRow>
            <DataRow label="Idempotency key">
              <span className="font-mono text-[11px]">{selectedTransfer.idempotencyKey}</span>
            </DataRow>
            {selectedTransfer.failureMessage && (
              <DataRow label="Failure">
                <span className="text-rose-600">{selectedTransfer.failureMessage}</span>
              </DataRow>
            )}
          </div>
            <Button
              variant="secondary"
              icon={Download}
              onClick={() =>
                exportReceiptPdf({
                  title: 'Transfer receipt',
                  subtitle: selectedTransfer.description || 'Ledger transfer',
                  rows: transferReceiptRows(selectedTransfer),
                  filename: `transfer-${selectedTransfer.id}`,
                  heroAmount: formatMoney(
                    (selectedTransfer.amountCents || 0) + (selectedTransfer.feeCents || 0),
                  ),
                  heroCaption: 'Total debited',
                })
              }
            >
              Export as PDF
            </Button>
          </div>
        )}
      </Modal>
    </AppLayout>
  );
}
