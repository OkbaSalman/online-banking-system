import React, { useCallback, useEffect, useState } from 'react';
import { CreditCard, Download, Lock, Plus, Receipt, SlidersHorizontal, Unlock, Wifi, Zap } from 'lucide-react';
import AppLayout from '../components/layout/AppLayout';
import AccountSelect from '../components/AccountSelect';
import {
  Button,
  Card,
  DataRow,
  EmptyState,
  ErrorNotice,
  InfoNotice,
  Modal,
  MoneyField,
  SectionTitle,
  Spinner,
  StatusBadge,
  TextField,
} from '../components/ui';
import { useAccounts, describeAccount } from '../hooks/useAccounts';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import * as cardsService from '../services/cardsService';
import { BRAND, TRANSFER_FEE_BPS } from '../config';
import {
  calculateFeeCents,
  centsToDollarsInput,
  displayNameFromEmail,
  dollarsToCents,
  formatDateTime,
  formatMoney,
  isUuid,
  optionalLimitCents,
} from '../lib/format';
import { cardChargeReceiptRows, exportReceiptPdf } from '../lib/receiptPdf';

function formatLimit(cents) {
  return cents > 0 ? formatMoney(cents) : 'No limit';
}

function VirtualCard({ card, holder, fundingLabel, selected, onSelect }) {
  const frozen = card.status === 'FROZEN';
  const closed = card.status === 'CLOSED';

  return (
    <button
      type="button"
      onClick={() => onSelect(card)}
      className={`relative w-full cursor-pointer overflow-hidden rounded-[1.75rem] p-6 text-left text-white shadow-xl transition-all duration-300 ${
        frozen || closed
          ? 'bg-gradient-to-br from-slate-600 to-slate-900 saturate-50'
          : 'bg-gradient-to-br from-[#1e1363] to-[#0b1c30]'
      } ${selected ? 'ring-2 ring-[#4f46e5] ring-offset-2 ring-offset-slate-50' : 'hover:-translate-y-0.5'}`}
    >
      <div className="pointer-events-none absolute right-0 top-0 h-64 w-64 rounded-full bg-indigo-500/10 blur-3xl" />

      <div className="relative flex items-start justify-between">
        <div>
          <span className="rounded-full bg-white/10 px-2 py-0.5 font-mono text-[9px] uppercase tracking-widest text-[#a59bff]">
            Virtual debit
          </span>
          <p className="mt-2 truncate text-sm font-bold">{card.nickname || 'Untitled card'}</p>
        </div>
        <div className="flex items-center gap-2">
          <Wifi size={16} className="rotate-90 text-slate-400" />
          <span className="text-sm font-extrabold tracking-widest">{BRAND.short.toUpperCase()}</span>
        </div>
      </div>

      <p className="relative mt-8 font-mono text-lg tracking-[0.2em] text-slate-200 sm:text-xl">
        •••• •••• •••• {card.last4 || '••••'}
      </p>

      <div className="relative mt-5 flex items-end justify-between gap-3">
        <div className="min-w-0">
          <p className="text-[8px] uppercase text-slate-400">Cardholder</p>
          <p className="truncate text-xs font-bold uppercase tracking-wider">{holder}</p>
        </div>
        <div className="min-w-0 text-right">
          <p className="text-[8px] uppercase text-slate-400">Funding account</p>
          <p className="truncate text-xs font-bold">{fundingLabel}</p>
        </div>
      </div>

      {(frozen || closed) && (
        <div className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-slate-950/60 backdrop-blur-[2px]">
          <Lock size={30} className="mb-2 text-rose-400" />
          <p className="font-mono text-xs font-black uppercase tracking-wider text-rose-300">
            {closed ? 'Card closed' : 'Card frozen'}
          </p>
        </div>
      )}
    </button>
  );
}

export default function CardsPage() {
  const { user } = useAuth();
  const toast = useToast();
  const { accounts, balances, loading: accountsLoading } = useAccounts();

  const [cards, setCards] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedCard, setSelectedCard] = useState(null);

  const [charges, setCharges] = useState([]);
  const [chargesLoading, setChargesLoading] = useState(false);

  const [showIssue, setShowIssue] = useState(false);
  const [fundingAccountId, setFundingAccountId] = useState('');
  const [nickname, setNickname] = useState('');
  const [issuing, setIssuing] = useState(false);
  const [issueError, setIssueError] = useState(null);
  const [dailyLimit, setDailyLimit] = useState('');
  const [monthlyLimit, setMonthlyLimit] = useState('');
  const [perTxnLimit, setPerTxnLimit] = useState('');

  const [showLimits, setShowLimits] = useState(false);
  const [editDailyLimit, setEditDailyLimit] = useState('');
  const [editMonthlyLimit, setEditMonthlyLimit] = useState('');
  const [editPerTxnLimit, setEditPerTxnLimit] = useState('');
  const [savingLimits, setSavingLimits] = useState(false);
  const [limitsError, setLimitsError] = useState(null);

  const [showCharge, setShowCharge] = useState(false);
  const [merchantAccountId, setMerchantAccountId] = useState('');
  const [chargeAmount, setChargeAmount] = useState('');
  const [chargeDescription, setChargeDescription] = useState('');
  const [charging, setCharging] = useState(false);
  const [chargeError, setChargeError] = useState(null);
  const [selectedCharge, setSelectedCharge] = useState(null);

  const holder = displayNameFromEmail(user?.email);
  const chargeAmountCents = dollarsToCents(chargeAmount);
  const chargeFeeCents = calculateFeeCents(chargeAmountCents || 0);
  const chargeTotalCents = (chargeAmountCents || 0) + chargeFeeCents;

  const loadCards = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await cardsService.listMyCards();
      setCards(list);
      setSelectedCard((current) => list.find((card) => card.id === current?.id) || list[0] || null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCards();
  }, [loadCards]);

  useEffect(() => {
    if (!selectedCard) {
      setCharges([]);
      return;
    }
    let cancelled = false;
    setChargesLoading(true);
    cardsService
      .listCardCharges(selectedCard.id, { limit: 25 })
      .then((list) => !cancelled && setCharges(list))
      .catch(() => !cancelled && setCharges([]))
      .finally(() => !cancelled && setChargesLoading(false));
    return () => {
      cancelled = true;
    };
  }, [selectedCard]);

  const fundingLabel = (card) => {
    const account = accounts.find((item) => item.id === card.fundingAccountId);
    return account ? describeAccount(account) : `••${String(card.fundingAccountId).slice(-4)}`;
  };

  const handleIssue = async (event) => {
    event.preventDefault();
    setIssueError(null);
    if (!fundingAccountId) return setIssueError('Choose the account that funds this card.');
    const dailyLimitCents = optionalLimitCents(dailyLimit);
    const monthlyLimitCents = optionalLimitCents(monthlyLimit);
    const perTransactionLimitCents = optionalLimitCents(perTxnLimit);
    if (dailyLimitCents == null || monthlyLimitCents == null || perTransactionLimitCents == null) {
      return setIssueError('Limits must be empty (unlimited) or a non-negative amount.');
    }

    setIssuing(true);
    try {
      const card = await cardsService.createCard(fundingAccountId, nickname.trim(), {
        dailyLimitCents,
        monthlyLimitCents,
        perTransactionLimitCents,
      });
      toast.success('Card issued', `Card ending ${card.last4} is active.`);
      setShowIssue(false);
      setNickname('');
      setDailyLimit('');
      setMonthlyLimit('');
      setPerTxnLimit('');
      await loadCards();
      setSelectedCard(card);
    } catch (err) {
      setIssueError(err.message);
    } finally {
      setIssuing(false);
    }
  };

  const toggleFreeze = async () => {
    if (!selectedCard) return;
    const frozen = selectedCard.status === 'FROZEN';
    try {
      const updated = frozen
        ? await cardsService.unfreezeCard(selectedCard.id)
        : await cardsService.freezeCard(selectedCard.id);
      toast.success(frozen ? 'Card unfrozen' : 'Card frozen', frozen ? 'Payments are allowed again.' : 'New charges will be declined.');
      setSelectedCard(updated);
      await loadCards();
    } catch (err) {
      toast.error('Could not update the card', err.message);
    }
  };

  const openLimits = () => {
    if (!selectedCard) return;
    setLimitsError(null);
    setEditDailyLimit(selectedCard.dailyLimitCents ? centsToDollarsInput(selectedCard.dailyLimitCents) : '');
    setEditMonthlyLimit(selectedCard.monthlyLimitCents ? centsToDollarsInput(selectedCard.monthlyLimitCents) : '');
    setEditPerTxnLimit(
      selectedCard.perTransactionLimitCents ? centsToDollarsInput(selectedCard.perTransactionLimitCents) : '',
    );
    setShowLimits(true);
  };

  const handleSaveLimits = async (event) => {
    event.preventDefault();
    if (!selectedCard) return;
    setLimitsError(null);
    const dailyLimitCents = optionalLimitCents(editDailyLimit);
    const monthlyLimitCents = optionalLimitCents(editMonthlyLimit);
    const perTransactionLimitCents = optionalLimitCents(editPerTxnLimit);
    if (dailyLimitCents == null || monthlyLimitCents == null || perTransactionLimitCents == null) {
      return setLimitsError('Limits must be empty (unlimited) or a non-negative amount.');
    }
    setSavingLimits(true);
    try {
      const updated = await cardsService.setCardLimits(selectedCard.id, {
        dailyLimitCents,
        monthlyLimitCents,
        perTransactionLimitCents,
      });
      toast.success('Limits updated', 'New purchases will use these spending caps.');
      setSelectedCard(updated);
      setShowLimits(false);
      await loadCards();
    } catch (err) {
      setLimitsError(err.message);
    } finally {
      setSavingLimits(false);
    }
  };

  const handleCharge = async (event) => {
    event.preventDefault();
    setChargeError(null);

    const amountCents = dollarsToCents(chargeAmount);
    if (!isUuid(merchantAccountId)) return setChargeError('Enter the merchant account id (UUID).');
    if (!amountCents) return setChargeError('Enter an amount greater than zero.');

    setCharging(true);
    try {
      const charge = await cardsService.chargeCard(selectedCard.id, {
        merchantAccountId: merchantAccountId.trim(),
        amountCents,
        description: chargeDescription.trim(),
      });
      const status = String(charge.status || '');
      if (status.includes('FAILED') || status.includes('BLOCKED')) {
        toast.error(
          'Charge declined',
          charge.failureMessage || status,
        );
      } else {
        toast.success(
          'Charge submitted',
          `${formatMoney(charge.amountCents)}${charge.feeCents ? ` + ${formatMoney(charge.feeCents)} fee` : ''} — ${charge.status}.`,
        );
      }
      setShowCharge(false);
      setChargeAmount('');
      setChargeDescription('');
      setCharges(await cardsService.listCardCharges(selectedCard.id, { limit: 25 }));
    } catch (err) {
      setChargeError(err.message);
    } finally {
      setCharging(false);
    }
  };

  return (
    <AppLayout
      eyebrow="Cards"
      title="Virtual cards"
      description="Issue a card against any of your accounts, freeze it instantly, and follow every charge it settles through the ledger."
      actions={
        <Button icon={Plus} onClick={() => setShowIssue(true)} disabled={!accounts.length}>
          Issue a card
        </Button>
      }
    >
      {error && (
        <div className="mb-6">
          <ErrorNotice>{error}</ErrorNotice>
        </div>
      )}

      {loading || accountsLoading ? (
        <Card>
          <Spinner label="Loading cards…" />
        </Card>
      ) : cards.length === 0 ? (
        <EmptyState
          icon={CreditCard}
          title="No cards yet"
          description={
            accounts.length
              ? 'Issue a virtual card against one of your accounts. Charges are debited from that account through the ledger.'
              : 'Open an account first — a card always needs a funding account behind it.'
          }
          action={
            accounts.length ? (
              <Button size="sm" icon={Plus} onClick={() => setShowIssue(true)}>
                Issue your first card
              </Button>
            ) : null
          }
        />
      ) : (
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
          <div className="space-y-5 lg:col-span-6">
            <SectionTitle icon={CreditCard} title={`Your cards (${cards.length})`} />
            <div className="space-y-4">
              {cards.map((card) => (
                <VirtualCard
                  key={card.id}
                  card={card}
                  holder={holder}
                  fundingLabel={fundingLabel(card)}
                  selected={selectedCard?.id === card.id}
                  onSelect={setSelectedCard}
                />
              ))}
            </div>
          </div>

          <div className="space-y-6 lg:col-span-6">
            {selectedCard && (
              <>
                <Card className="p-6">
                  <SectionTitle
                    icon={Zap}
                    title="Card controls"
                    description={`Ending ${selectedCard.last4} · issued ${formatDateTime(selectedCard.createdAtEpochMs)}`}
                    action={<StatusBadge status={selectedCard.status} />}
                  />

                  <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                    <Button
                      variant={selectedCard.status === 'FROZEN' ? 'primary' : 'danger'}
                      icon={selectedCard.status === 'FROZEN' ? Unlock : Lock}
                      onClick={toggleFreeze}
                      disabled={selectedCard.status === 'CLOSED'}
                    >
                      {selectedCard.status === 'FROZEN' ? 'Unfreeze card' : 'Freeze card'}
                    </Button>
                    <Button
                      variant="secondary"
                      icon={Receipt}
                      onClick={() => {
                        setChargeError(null);
                        setShowCharge(true);
                      }}
                      disabled={selectedCard.status !== 'ACTIVE'}
                    >
                      Simulate a purchase
                    </Button>
                    <Button
                      variant="secondary"
                      icon={SlidersHorizontal}
                      onClick={openLimits}
                      disabled={selectedCard.status === 'CLOSED'}
                      className="sm:col-span-2"
                    >
                      Edit spending limits
                    </Button>
                  </div>

                  <dl className="mt-5 grid grid-cols-1 gap-3 sm:grid-cols-3">
                    {[
                      ['Per purchase', selectedCard.perTransactionLimitCents],
                      ['Daily', selectedCard.dailyLimitCents],
                      ['Monthly', selectedCard.monthlyLimitCents],
                    ].map(([label, cents]) => (
                      <div key={label} className="rounded-2xl bg-slate-50 px-3 py-2.5">
                        <dt className="text-[10px] font-bold uppercase tracking-widest text-[#777587]">{label}</dt>
                        <dd className="mt-1 font-mono text-xs font-black text-[#0b1c30]">{formatLimit(cents)}</dd>
                      </div>
                    ))}
                  </dl>

                  <div className="mt-5">
                    <InfoNotice icon={Lock} title="Limits apply to the purchase amount:">
                      Leave a field empty for no cap. The transfer fee is charged on top of the
                      purchase and does not count toward these limits.
                    </InfoNotice>
                  </div>
                </Card>

                <Card className="p-6">
                  <SectionTitle icon={Receipt} title="Charges" description="Every purchase made with this card." />

                  {chargesLoading ? (
                    <Spinner label="Loading charges…" />
                  ) : charges.length === 0 ? (
                    <EmptyState
                      icon={Receipt}
                      title="No charges yet"
                      description="Purchases made with this card will be listed here with their ledger status."
                    />
                  ) : (
                    <div className="divide-y divide-slate-100">
                      {charges.map((charge) => (
                        <button
                          key={charge.id}
                          type="button"
                          onClick={() => setSelectedCharge(charge)}
                          className="flex w-full cursor-pointer items-center justify-between gap-4 py-3 text-left first:pt-0 last:pb-0 hover:bg-slate-50/80"
                        >
                          <div className="min-w-0">
                            <p className="truncate text-xs font-bold text-[#0b1c30]">
                              {charge.description || 'Card purchase'}
                            </p>
                            <div className="mt-1 flex flex-wrap items-center gap-2">
                              <StatusBadge status={charge.status} />
                              <span className="font-mono text-[10px] text-slate-400">
                                {formatDateTime(charge.createdAtEpochMs)}
                              </span>
                            </div>
                            {charge.feeCents > 0 && (
                              <p className="mt-1 text-[11px] text-slate-500">
                                Fee {formatMoney(charge.feeCents)}
                              </p>
                            )}
                            {charge.failureMessage && (
                              <p className="mt-1 text-[11px] text-rose-600">{charge.failureMessage}</p>
                            )}
                          </div>
                          <span className="shrink-0 font-mono text-sm font-black text-rose-600">
                            -{formatMoney((charge.amountCents || 0) + (charge.feeCents || 0))}
                          </span>
                        </button>
                      ))}
                    </div>
                  )}
                </Card>
              </>
            )}
          </div>
        </div>
      )}

      <Modal
        open={showIssue}
        onClose={() => setShowIssue(false)}
        title="Issue a virtual card"
        description="Charges on this card are debited from the funding account you pick."
      >
        <form onSubmit={handleIssue} className="space-y-4">
          <ErrorNotice>{issueError}</ErrorNotice>

          <AccountSelect
            label="Funding account"
            accounts={accounts}
            balances={balances}
            value={fundingAccountId}
            onChange={(event) => setFundingAccountId(event.target.value)}
            required
          />

          <TextField
            label="Card name (optional)"
            value={nickname}
            onChange={(event) => setNickname(event.target.value)}
            placeholder="Online subscriptions"
            maxLength={40}
          />

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
            <MoneyField
              label="Per purchase"
              value={perTxnLimit}
              onChange={(event) => setPerTxnLimit(event.target.value)}
              placeholder="Unlimited"
              hint="Empty = no cap"
            />
            <MoneyField
              label="Daily"
              value={dailyLimit}
              onChange={(event) => setDailyLimit(event.target.value)}
              placeholder="Unlimited"
              hint="Empty = no cap"
            />
            <MoneyField
              label="Monthly"
              value={monthlyLimit}
              onChange={(event) => setMonthlyLimit(event.target.value)}
              placeholder="Unlimited"
              hint="Empty = no cap"
            />
          </div>

          <Button type="submit" size="lg" loading={issuing} icon={CreditCard}>
            Issue card
          </Button>
        </form>
      </Modal>

      <Modal
        open={showCharge}
        onClose={() => setShowCharge(false)}
        title="Simulate a purchase"
        description="Charges settle as a transfer from the funding account to the merchant account."
      >
        <form onSubmit={handleCharge} className="space-y-4">
          <ErrorNotice>{chargeError}</ErrorNotice>

          <TextField
            label="Merchant account id"
            value={merchantAccountId}
            onChange={(event) => setMerchantAccountId(event.target.value)}
            placeholder="00000000-0000-0000-0000-000000000000"
            className="font-mono text-xs"
            required
          />

          <MoneyField
            label="Amount"
            value={chargeAmount}
            onChange={(event) => setChargeAmount(event.target.value)}
            placeholder="0.00"
            required
          />

          {chargeAmountCents ? (
            <div className="space-y-1.5 rounded-2xl bg-[#eff4ff] p-4 text-xs">
              <div className="flex justify-between text-[#464555]">
                <span>Purchase</span>
                <span className="font-semibold text-[#0b1c30]">{formatMoney(chargeAmountCents)}</span>
              </div>
              <div className="flex justify-between text-[#464555]">
                <span>Fee ({(TRANSFER_FEE_BPS / 100).toFixed(2)}%)</span>
                <span className="font-semibold text-[#0b1c30]">{formatMoney(chargeFeeCents)}</span>
              </div>
              <div className="mt-2 flex justify-between border-t border-[#c7c4d8]/50 pt-2 text-sm">
                <span className="font-bold text-[#0b1c30]">Total debited</span>
                <span className="font-black text-[#3525cd]">{formatMoney(chargeTotalCents)}</span>
              </div>
            </div>
          ) : null}

          <TextField
            label="Description (optional)"
            value={chargeDescription}
            onChange={(event) => setChargeDescription(event.target.value)}
            placeholder="Coffee shop"
            maxLength={140}
          />

          <Button type="submit" size="lg" loading={charging} icon={Zap}>
            Charge card
          </Button>
        </form>
      </Modal>

      <Modal
        open={showLimits}
        onClose={() => setShowLimits(false)}
        title="Spending limits"
        description="Empty fields mean unlimited. Limits apply to the purchase amount, not the fee."
      >
        <form onSubmit={handleSaveLimits} className="space-y-4">
          <ErrorNotice>{limitsError}</ErrorNotice>
          <MoneyField
            label="Per purchase"
            value={editPerTxnLimit}
            onChange={(event) => setEditPerTxnLimit(event.target.value)}
            placeholder="Unlimited"
          />
          <MoneyField
            label="Daily"
            value={editDailyLimit}
            onChange={(event) => setEditDailyLimit(event.target.value)}
            placeholder="Unlimited"
          />
          <MoneyField
            label="Monthly"
            value={editMonthlyLimit}
            onChange={(event) => setEditMonthlyLimit(event.target.value)}
            placeholder="Unlimited"
          />
          <Button type="submit" size="lg" loading={savingLimits} icon={SlidersHorizontal}>
            Save limits
          </Button>
        </form>
      </Modal>

      <Modal
        open={Boolean(selectedCharge)}
        onClose={() => setSelectedCharge(null)}
        title="Card charge receipt"
        description="Purchase plus the transfer fee taken from the funding account."
      >
        {selectedCharge && (
          <div className="space-y-4">
            <div className="divide-y divide-slate-100 rounded-2xl border border-slate-100 bg-slate-50 p-4">
              <DataRow label="Status">
                <StatusBadge status={selectedCharge.status} />
              </DataRow>
              <DataRow label="Purchase">{formatMoney(selectedCharge.amountCents)}</DataRow>
              <DataRow label="Fee">{formatMoney(selectedCharge.feeCents)}</DataRow>
              <DataRow label="Total debited">
                {formatMoney((selectedCharge.amountCents || 0) + (selectedCharge.feeCents || 0))}
              </DataRow>
              <DataRow label="Merchant">
                <span className="font-mono text-[11px]">{selectedCharge.merchantAccountId}</span>
              </DataRow>
              <DataRow label="Reference">{selectedCharge.description || '—'}</DataRow>
              <DataRow label="Booked">{formatDateTime(selectedCharge.createdAtEpochMs)}</DataRow>
            </div>
            <Button
              variant="secondary"
              icon={Download}
              onClick={() =>
                exportReceiptPdf({
                  title: 'Card charge receipt',
                  subtitle: selectedCharge.description || 'Virtual card purchase',
                  rows: cardChargeReceiptRows(selectedCharge),
                  filename: `card-charge-${selectedCharge.id}`,
                  heroAmount: formatMoney(
                    (selectedCharge.amountCents || 0) + (selectedCharge.feeCents || 0),
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
