import React, { useCallback, useEffect, useState } from 'react';
import { CalendarClock, Download, Inbox, Receipt, RefreshCw, Repeat, Send, Ban } from 'lucide-react';
import AppLayout from '../components/layout/AppLayout';
import AccountSelect from '../components/AccountSelect';
import {
  Button,
  Card,
  DataRow,
  EmptyState,
  ErrorNotice,
  Modal,
  SectionTitle,
  SelectField,
  Spinner,
  StatusBadge,
  TextField,
  MoneyField,
} from '../components/ui';
import { useAccounts } from '../hooks/useAccounts';
import { useToast } from '../context/ToastContext';
import * as billingService from '../services/billingService';
import { dollarsToCents, formatDateTime, formatMoney, isUuid, shortId } from '../lib/format';
import { billingPaymentReceiptRows, exportReceiptPdf } from '../lib/receiptPdf';

const INTERVALS = [
  { value: 'DAY', label: 'Day' },
  { value: 'WEEK', label: 'Week' },
  { value: 'MONTH', label: 'Month' },
];

export default function BillingPage() {
  const toast = useToast();
  const { accounts, balances, loading: accountsLoading, reload } = useAccounts();

  const [payments, setPayments] = useState([]);
  const [subscriptions, setSubscriptions] = useState([]);
  const [listsLoading, setListsLoading] = useState(true);

  const [payForm, setPayForm] = useState({
    fromAccountId: '',
    merchantAccountId: '',
    amount: '',
    description: '',
  });
  const [paying, setPaying] = useState(false);
  const [payError, setPayError] = useState(null);

  const [subForm, setSubForm] = useState({
    fromAccountId: '',
    merchantAccountId: '',
    amount: '',
    intervalUnit: 'MONTH',
    intervalCount: 1,
    description: '',
  });
  const [subscribing, setSubscribing] = useState(false);
  const [subError, setSubError] = useState(null);
  const [selectedPayment, setSelectedPayment] = useState(null);

  const loadLists = useCallback(async () => {
    setListsLoading(true);
    const [paymentsResult, subscriptionsResult] = await Promise.allSettled([
      billingService.listPayments({ limit: 25 }),
      billingService.listSubscriptions({ limit: 25 }),
    ]);
    if (paymentsResult.status === 'fulfilled') setPayments(paymentsResult.value);
    if (subscriptionsResult.status === 'fulfilled') setSubscriptions(subscriptionsResult.value);
    setListsLoading(false);
  }, []);

  useEffect(() => {
    loadLists();
  }, [loadLists]);

  useEffect(() => {
    if (!accounts.length) return;
    setPayForm((form) => (form.fromAccountId ? form : { ...form, fromAccountId: accounts[0].id }));
    setSubForm((form) => (form.fromAccountId ? form : { ...form, fromAccountId: accounts[0].id }));
  }, [accounts]);

  const handlePay = async (event) => {
    event.preventDefault();
    setPayError(null);

    const amountCents = dollarsToCents(payForm.amount);
    if (!payForm.fromAccountId) return setPayError('Choose the account to pay from.');
    if (!isUuid(payForm.merchantAccountId)) return setPayError('Enter the merchant account id (UUID).');
    if (!amountCents) return setPayError('Enter an amount greater than zero.');

    setPaying(true);
    try {
      const payment = await billingService.payBill({
        fromAccountId: payForm.fromAccountId,
        merchantAccountId: payForm.merchantAccountId.trim(),
        amountCents,
        description: payForm.description.trim(),
      });
      toast.success('Bill paid', `${formatMoney(payment.amountCents)} — ${payment.status}.`);
      setPayForm((form) => ({ ...form, amount: '', description: '' }));
      await Promise.all([loadLists(), reload()]);
    } catch (err) {
      setPayError(err.message);
    } finally {
      setPaying(false);
    }
  };

  const handleSubscribe = async (event) => {
    event.preventDefault();
    setSubError(null);

    const amountCents = dollarsToCents(subForm.amount);
    if (!subForm.fromAccountId) return setSubError('Choose the account to charge.');
    if (!isUuid(subForm.merchantAccountId)) return setSubError('Enter the merchant account id (UUID).');
    if (!amountCents) return setSubError('Enter an amount greater than zero.');

    setSubscribing(true);
    try {
      const subscription = await billingService.createSubscription({
        fromAccountId: subForm.fromAccountId,
        merchantAccountId: subForm.merchantAccountId.trim(),
        amountCents,
        intervalUnit: subForm.intervalUnit,
        intervalCount: Number(subForm.intervalCount) || 1,
        startAtEpochMs: Date.now(),
        description: subForm.description.trim(),
      });
      toast.success(
        'Subscription started',
        `First charge taken. Next charge ${formatDateTime(subscription.nextChargeAtEpochMs)}.`,
      );
      setSubForm((form) => ({ ...form, amount: '', description: '' }));
      await Promise.all([loadLists(), reload()]);
    } catch (err) {
      setSubError(err.message);
      await loadLists();
    } finally {
      setSubscribing(false);
    }
  };

  const cancelSubscription = async (subscriptionId) => {
    try {
      await billingService.cancelSubscription(subscriptionId);
      toast.info('Subscription cancelled', 'No further charges will be taken.');
      await loadLists();
    } catch (err) {
      toast.error('Could not cancel the subscription', err.message);
    }
  };

  return (
    <AppLayout
      eyebrow="Bills"
      title="Bills & subscriptions"
      description="One-off merchant payments and recurring charges, both settled through the same ledger as your transfers."
      actions={
        <Button variant="secondary" icon={RefreshCw} onClick={loadLists}>
          Refresh
        </Button>
      }
    >
      {accountsLoading ? (
        <Card>
          <Spinner label="Loading accounts…" />
        </Card>
      ) : accounts.length === 0 ? (
        <EmptyState
          icon={Receipt}
          title="No account to pay from"
          description="Open an account first, then you can pay merchants and set up subscriptions."
        />
      ) : (
        <>
          <div className="grid grid-cols-1 gap-8 lg:grid-cols-2">
            <Card className="p-6 sm:p-8">
              <SectionTitle
                icon={Receipt}
                title="Pay a bill"
                description="A single payment to a merchant account."
              />

              <form onSubmit={handlePay} className="space-y-4">
                <ErrorNotice>{payError}</ErrorNotice>

                <AccountSelect
                  label="Pay from"
                  accounts={accounts}
                  balances={balances}
                  value={payForm.fromAccountId}
                  onChange={(event) => setPayForm({ ...payForm, fromAccountId: event.target.value })}
                  required
                />

                <TextField
                  label="Merchant account id"
                  value={payForm.merchantAccountId}
                  onChange={(event) => setPayForm({ ...payForm, merchantAccountId: event.target.value })}
                  placeholder="00000000-0000-0000-0000-000000000000"
                  className="font-mono text-xs"
                  required
                />

                <MoneyField
                  label="Amount"
                  value={payForm.amount}
                  onChange={(event) => setPayForm({ ...payForm, amount: event.target.value })}
                  placeholder="0.00"
                  required
                />

                <TextField
                  label="Reference (optional)"
                  value={payForm.description}
                  onChange={(event) => setPayForm({ ...payForm, description: event.target.value })}
                  placeholder="Electricity — March"
                  maxLength={140}
                />

                <Button type="submit" size="lg" loading={paying} icon={Send}>
                  Pay now
                </Button>
              </form>
            </Card>

            <Card className="p-6 sm:p-8">
              <SectionTitle
                icon={Repeat}
                title="New subscription"
                description="The first charge is taken now. Later charges follow the interval you set."
              />

              <form onSubmit={handleSubscribe} className="space-y-4">
                <ErrorNotice>{subError}</ErrorNotice>

                <AccountSelect
                  label="Charge account"
                  accounts={accounts}
                  balances={balances}
                  value={subForm.fromAccountId}
                  onChange={(event) => setSubForm({ ...subForm, fromAccountId: event.target.value })}
                  required
                />

                <TextField
                  label="Merchant account id"
                  value={subForm.merchantAccountId}
                  onChange={(event) => setSubForm({ ...subForm, merchantAccountId: event.target.value })}
                  placeholder="00000000-0000-0000-0000-000000000000"
                  className="font-mono text-xs"
                  required
                />

                <MoneyField
                  label="Amount per charge"
                  value={subForm.amount}
                  onChange={(event) => setSubForm({ ...subForm, amount: event.target.value })}
                  placeholder="0.00"
                  required
                />

                <div className="grid grid-cols-2 gap-3">
                  <TextField
                    label="Every"
                    type="number"
                    min="1"
                    value={subForm.intervalCount}
                    onChange={(event) => setSubForm({ ...subForm, intervalCount: event.target.value })}
                    required
                  />
                  <SelectField
                    label="Interval"
                    value={subForm.intervalUnit}
                    onChange={(event) => setSubForm({ ...subForm, intervalUnit: event.target.value })}
                  >
                    {INTERVALS.map((interval) => (
                      <option key={interval.value} value={interval.value}>
                        {interval.label}
                      </option>
                    ))}
                  </SelectField>
                </div>

                <TextField
                  label="Description (optional)"
                  value={subForm.description}
                  onChange={(event) => setSubForm({ ...subForm, description: event.target.value })}
                  placeholder="Streaming plan"
                  maxLength={140}
                />

                <Button type="submit" size="lg" loading={subscribing} icon={Repeat}>
                  Create subscription & pay first charge
                </Button>
              </form>
            </Card>
          </div>

          <Card className="mt-8 p-6 sm:p-8">
            <SectionTitle
              icon={CalendarClock}
              title={`Active subscriptions (${subscriptions.length})`}
              description="Recurring charges billing-service will take on your behalf."
            />

            {listsLoading ? (
              <Spinner />
            ) : subscriptions.length === 0 ? (
              <EmptyState
                icon={Repeat}
                title="No subscriptions"
                description="Recurring merchant charges you set up will be listed here."
              />
            ) : (
              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                {subscriptions.map((subscription) => (
                  <div
                    key={subscription.id}
                    className="rounded-2xl border border-slate-100 bg-slate-50/70 p-4"
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="truncate text-sm font-bold text-[#0b1c30]">
                          {subscription.description || 'Subscription'}
                        </p>
                        <p className="mt-0.5 font-mono text-[10px] text-slate-400">
                          Merchant {shortId(subscription.merchantAccountId)}
                        </p>
                      </div>
                      <StatusBadge status={subscription.status} />
                    </div>

                    <p className="mt-3 text-xl font-black text-[#0b1c30]">
                      {formatMoney(subscription.amountCents)}
                      <span className="ml-1 text-[11px] font-semibold text-[#777587]">
                        every {subscription.intervalCount} {subscription.intervalUnit?.toLowerCase()}
                        {subscription.intervalCount > 1 ? 's' : ''}
                      </span>
                    </p>

                    <p className="mt-1 text-[11px] text-[#777587]">
                      Next charge {formatDateTime(subscription.nextChargeAtEpochMs)}
                    </p>

                    {subscription.status === 'ACTIVE' && (
                      <Button
                        variant="danger"
                        size="sm"
                        icon={Ban}
                        className="mt-3"
                        onClick={() => cancelSubscription(subscription.id)}
                      >
                        Cancel
                      </Button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </Card>

          <Card className="mt-8 p-6 sm:p-8">
            <SectionTitle icon={Receipt} title="Payment history" description="Bills and subscription charges." />

            {listsLoading ? (
              <Spinner />
            ) : payments.length === 0 ? (
              <EmptyState
                icon={Inbox}
                title="No payments yet"
                description="Bills you pay and subscription charges taken on your behalf appear here."
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full min-w-[680px] border-collapse text-left">
                  <thead>
                    <tr className="border-b border-slate-100">
                      {['Date', 'Reference', 'Merchant', 'Source', 'Status', 'Amount', ''].map((heading, index) => (
                        <th
                          key={heading || 'receipt'}
                          className={`py-3 font-mono text-[10px] font-bold uppercase tracking-widest text-slate-400 ${
                            index >= 5 ? 'text-right' : ''
                          }`}
                        >
                          {heading}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-50 text-xs">
                    {payments.map((payment) => (
                      <tr key={payment.id} className="transition-colors hover:bg-slate-50/70">
                        <td className="whitespace-nowrap py-4 font-mono text-[11px] text-slate-500">
                          {formatDateTime(payment.createdAtEpochMs)}
                        </td>
                        <td className="max-w-[14rem] truncate py-4 font-bold text-[#0b1c30]">
                          {payment.description || 'Payment'}
                          {payment.subscriptionId && (
                            <span className="ml-2 text-[10px] font-semibold uppercase text-[#4f46e5]">
                              recurring
                            </span>
                          )}
                        </td>
                        <td className="whitespace-nowrap py-4 font-mono text-[11px] text-slate-400">
                          {shortId(payment.merchantAccountId)}
                        </td>
                        <td className="whitespace-nowrap py-4 font-mono text-[11px] text-slate-400">
                          {shortId(payment.fromAccountId)}
                        </td>
                        <td className="py-4">
                          <StatusBadge status={payment.status} />
                        </td>
                        <td className="whitespace-nowrap py-4 text-right font-mono font-black text-rose-600">
                          -{formatMoney(payment.amountCents)}
                        </td>
                        <td className="whitespace-nowrap py-4 text-right">
                          <button
                            type="button"
                            onClick={() => setSelectedPayment(payment)}
                            className="cursor-pointer rounded-lg bg-[#eff4ff] px-3 py-1.5 text-[10px] font-black uppercase text-[#3525cd] transition-colors hover:bg-[#3525cd] hover:text-white"
                          >
                            Receipt
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Card>
        </>
      )}

      <Modal
        open={Boolean(selectedPayment)}
        onClose={() => setSelectedPayment(null)}
        title="Payment receipt"
        description="The record as stored by billing-service."
      >
        {selectedPayment && (
          <div className="space-y-4">
            <div className="divide-y divide-slate-100 rounded-2xl border border-slate-100 bg-slate-50 p-4">
              <DataRow label="Status">
                <StatusBadge status={selectedPayment.status} />
              </DataRow>
              <DataRow label="Amount">{formatMoney(selectedPayment.amountCents)}</DataRow>
              <DataRow label="From">
                <span className="font-mono text-[11px]">{selectedPayment.fromAccountId}</span>
              </DataRow>
              <DataRow label="Merchant">
                <span className="font-mono text-[11px]">{selectedPayment.merchantAccountId}</span>
              </DataRow>
              <DataRow label="Reference">{selectedPayment.description || '—'}</DataRow>
              <DataRow label="Booked">{formatDateTime(selectedPayment.createdAtEpochMs)}</DataRow>
            </div>
            <Button
              variant="secondary"
              icon={Download}
              onClick={() =>
                exportReceiptPdf({
                  title: 'Payment receipt',
                  subtitle: selectedPayment.description || 'Bill payment',
                  rows: billingPaymentReceiptRows(selectedPayment),
                  filename: `payment-${selectedPayment.id}`,
                  heroAmount: formatMoney(selectedPayment.amountCents),
                  heroCaption: 'Amount paid',
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
