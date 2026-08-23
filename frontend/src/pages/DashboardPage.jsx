import React, { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRightLeft,
  ArrowUpRight,
  Check,
  Inbox,
  Landmark,
  Mail,
  Plus,
  ReceiptText,
  ShieldAlert,
  ShieldCheck,
  Snowflake,
  X,
} from 'lucide-react';
import AppLayout from '../components/layout/AppLayout';
import CreateAccountModal from '../components/modals/CreateAccountModal';
import TransferModal from '../components/modals/TransferModal';
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorNotice,
  SectionTitle,
  Spinner,
  StatusBadge,
} from '../components/ui';
import { useAccounts, describeAccount } from '../hooks/useAccounts';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import * as accountsService from '../services/accountsService';
import { listMyTransfers } from '../services/transfersService';
import { getMyKyc } from '../services/kycService';
import {
  accountTypeLabel,
  displayNameFromEmail,
  formatDateTime,
  formatMoney,
  isSavings,
  kycStatusLabel,
  maskIban,
  membershipRoleLabel,
} from '../lib/format';

function KycBanner({ status }) {
  if (!status || status.includes('APPROVED')) return null;

  const config = status.includes('PENDING')
    ? {
        tone: 'border-amber-200 bg-amber-50 text-amber-900',
        icon: ShieldCheck,
        title: 'Identity check under review',
        body: 'A reviewer is looking at your documents. Transfers stay limited until it is approved.',
        cta: 'View submission',
      }
    : status.includes('REJECTED')
      ? {
          tone: 'border-rose-200 bg-rose-50 text-rose-900',
          icon: ShieldAlert,
          title: 'Identity check was rejected',
          body: 'Correct the details and resubmit your documents to restore full access.',
          cta: 'Resubmit documents',
        }
      : {
          tone: 'border-indigo-200 bg-[#eff4ff] text-[#0b1c30]',
          icon: ShieldAlert,
          title: 'Verify your identity',
          body: 'Submit an ID document and address before moving money out of your accounts.',
          cta: 'Start verification',
        };

  const Icon = config.icon;

  return (
    <div className={`mb-8 flex flex-col gap-3 rounded-2xl border p-5 sm:flex-row sm:items-center sm:justify-between ${config.tone}`}>
      <div className="flex items-start gap-3">
        <Icon size={20} className="mt-0.5 shrink-0" />
        <div>
          <p className="text-sm font-bold">{config.title}</p>
          <p className="mt-0.5 text-xs opacity-90">{config.body}</p>
        </div>
      </div>
      <Link to="/compliance" className="shrink-0">
        <Button variant="secondary" size="sm">
          {config.cta}
        </Button>
      </Link>
    </div>
  );
}

export default function DashboardPage() {
  const { user } = useAuth();
  const toast = useToast();
  const { accounts, balances, totalCents, loading, error, reload } = useAccounts();

  const [transfers, setTransfers] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [kycStatus, setKycStatus] = useState(null);
  const [activityLoading, setActivityLoading] = useState(true);

  const [showCreateAccount, setShowCreateAccount] = useState(false);
  const [showTransfer, setShowTransfer] = useState(false);

  const loadActivity = useCallback(async () => {
    setActivityLoading(true);
    const [transfersResult, invitationsResult, kycResult] = await Promise.allSettled([
      listMyTransfers({ limit: 8 }),
      accountsService.listMyInvitations(),
      getMyKyc(),
    ]);

    if (transfersResult.status === 'fulfilled') setTransfers(transfersResult.value);
    if (kycResult.status === 'fulfilled') setKycStatus(kycResult.value?.status || 'KYC_STATUS_NOT_SUBMITTED');

    if (invitationsResult.status === 'fulfilled') {
      setInvitations(
        invitationsResult.value.map((invitation) => ({
          ...invitation,
          inviterEmail: invitation.invitedByEmail || null,
        })),
      );
    }

    setActivityLoading(false);
  }, []);

  useEffect(() => {
    loadActivity();
  }, [loadActivity]);

  const refreshAll = useCallback(async () => {
    await Promise.all([reload(), loadActivity()]);
  }, [reload, loadActivity]);

  const respondToInvitation = async (invitationId, accept) => {
    try {
      if (accept) {
        await accountsService.acceptInvitation(invitationId);
        toast.success('Invitation accepted', 'The shared account now appears in your list.');
      } else {
        await accountsService.declineInvitation(invitationId);
        toast.info('Invitation declined');
      }
      await refreshAll();
    } catch (err) {
      toast.error('Could not update the invitation', err.message);
    }
  };

  const myAccountIds = new Set(accounts.map((account) => account.id));

  return (
    <AppLayout
      eyebrow="Overview"
      title={`Hello, ${displayNameFromEmail(user?.email)}`}
      description="Your balances, recent movements and anything waiting on you."
      actions={
        <>
          <Button icon={ArrowRightLeft} onClick={() => setShowTransfer(true)} disabled={!accounts.length}>
            Send money
          </Button>
          <Button variant="secondary" icon={Plus} onClick={() => setShowCreateAccount(true)}>
            Open account
          </Button>
        </>
      }
    >
      {error && (
        <div className="mb-6">
          <ErrorNotice>{error}</ErrorNotice>
        </div>
      )}

      <KycBanner status={kycStatus} />

      <section className="relative mb-8 overflow-hidden rounded-[2rem] bg-gradient-to-r from-[#0b1c30] to-[#1e1363] p-6 text-white shadow-2xl sm:p-8">
        <div className="pointer-events-none absolute right-0 top-0 h-96 w-96 rounded-full bg-[#4f46e5]/10 blur-3xl" />
        <div className="relative flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p className="font-mono text-[10px] font-bold uppercase tracking-[0.2em] text-[#a59bff] sm:text-xs">
              Total available balance
            </p>
            <h3 className="mt-2 text-4xl font-black tracking-tighter sm:text-5xl">
              {loading ? '—' : formatMoney(totalCents)}
            </h3>
            <p className="mt-2 text-xs text-slate-400">
              Across {accounts.length} {accounts.length === 1 ? 'account' : 'accounts'} · updated{' '}
              {new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })}
            </p>
          </div>

          <div className="flex flex-wrap gap-6">
            <div>
              <p className="text-[10px] uppercase tracking-widest text-slate-400">Checking</p>
              <p className="mt-1 text-lg font-bold">
                {formatMoney(
                  accounts
                    .filter((account) => !isSavings(account))
                    .reduce((sum, account) => sum + (balances[account.id] || 0), 0),
                )}
              </p>
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-widest text-slate-400">Savings</p>
              <p className="mt-1 text-lg font-bold">
                {formatMoney(
                  accounts
                    .filter(isSavings)
                    .reduce((sum, account) => sum + (balances[account.id] || 0), 0),
                )}
              </p>
            </div>
          </div>
        </div>
      </section>

      {invitations.length > 0 && (
        <Card className="mb-8 border-amber-200 bg-amber-50/60 p-5">
          <h4 className="mb-3 flex items-center gap-2 font-bold text-amber-900">
            <Mail size={18} />
            <span>
              {invitations.length} account {invitations.length === 1 ? 'invitation' : 'invitations'} waiting
            </span>
          </h4>
          <div className="space-y-3">
            {invitations.map((invitation) => (
              <div
                key={invitation.id}
                className="flex flex-col items-start justify-between gap-3 rounded-xl border border-amber-100 bg-white p-4 shadow-sm sm:flex-row sm:items-center"
              >
                <div className="min-w-0">
                  <p className="text-sm font-bold text-[#0b1c30]">
                    Join as {membershipRoleLabel(invitation.role)}
                  </p>
                  <p className="mt-0.5 break-all text-[11px] text-[#464555]">
                    {invitation.inviterEmail
                      ? `Invited by ${invitation.inviterEmail}`
                      : 'Invitation from an account owner'}
                  </p>
                  <p className="mt-0.5 break-all font-mono text-[10px] text-slate-400">
                    Account {invitation.accountId}
                  </p>
                  <p className="mt-0.5 text-[11px] text-slate-500">
                    Expires {formatDateTime(invitation.expiresAtEpochMs)}
                  </p>
                </div>
                <div className="flex shrink-0 gap-2">
                  <Button size="sm" icon={Check} onClick={() => respondToInvitation(invitation.id, true)}>
                    Accept
                  </Button>
                  <Button
                    size="sm"
                    variant="secondary"
                    icon={X}
                    onClick={() => respondToInvitation(invitation.id, false)}
                  >
                    Decline
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
        <div className="lg:col-span-5">
          <SectionTitle icon={Landmark} title="Your accounts" />

          {loading ? (
            <Card>
              <Spinner label="Loading accounts…" />
            </Card>
          ) : accounts.length === 0 ? (
            <EmptyState
              icon={Landmark}
              title="No accounts yet"
              description="Open a checking account to receive money, or a savings account to earn monthly interest."
              action={
                <Button size="sm" icon={Plus} onClick={() => setShowCreateAccount(true)}>
                  Open your first account
                </Button>
              }
            />
          ) : (
            <div className="space-y-4">
              {accounts.map((account) => (
                <Card key={account.id} className="p-5 transition-shadow hover:shadow-[0_18px_45px_rgba(11,28,48,0.08)]">
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <Badge tone={isSavings(account) ? 'amber' : 'indigo'}>
                          {accountTypeLabel(account.accountType)}
                        </Badge>
                        {account.frozen && (
                          <Badge tone="rose">
                            <Snowflake size={9} /> Frozen
                          </Badge>
                        )}
                      </div>
                      <h5 className="mt-2 truncate text-base font-bold text-[#0b1c30]">
                        {describeAccount(account)}
                      </h5>
                      <p className="mt-1 select-all font-mono text-[10px] text-slate-400">
                        {maskIban(account.iban)}
                      </p>
                    </div>
                  </div>

                  <div className="mt-4 flex items-end justify-between">
                    <p className="text-2xl font-black text-[#0b1c30]">
                      {balances[account.id] === null ? '—' : formatMoney(balances[account.id])}
                    </p>
                    <Link
                      to={`/accounts?account=${account.id}`}
                      className="flex items-center gap-1 text-xs font-semibold text-[#4f46e5] hover:underline"
                    >
                      Details <ArrowUpRight size={14} />
                    </Link>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>

        <div className="lg:col-span-7">
          <SectionTitle
            icon={ReceiptText}
            title="Recent transfers"
            action={
              <Link to="/payments" className="text-xs font-semibold text-[#4f46e5] hover:underline">
                View all
              </Link>
            }
          />

          <Card className="p-6 sm:p-8">
            {activityLoading ? (
              <Spinner label="Loading activity…" />
            ) : transfers.length === 0 ? (
              <EmptyState
                icon={Inbox}
                title="Nothing has moved yet"
                description="Transfers you send or receive will show up here with their ledger reference."
              />
            ) : (
              <div className="divide-y divide-slate-100">
                {transfers.map((transfer) => {
                  const incoming = myAccountIds.has(transfer.toAccountId);
                  return (
                    <div key={transfer.id} className="flex items-center justify-between gap-4 py-4 first:pt-0 last:pb-0">
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-bold text-[#0b1c30]">
                          {transfer.description || (incoming ? 'Incoming transfer' : 'Outgoing transfer')}
                        </p>
                        <div className="mt-1 flex flex-wrap items-center gap-2">
                          <Badge tone={incoming ? 'emerald' : 'neutral'}>
                            {incoming ? 'Received' : 'Sent'}
                          </Badge>
                          <StatusBadge status={transfer.status} />
                          <span className="font-mono text-[10px] text-slate-400">
                            {formatDateTime(transfer.createdAtEpochMs)}
                          </span>
                        </div>
                        {transfer.failureMessage && (
                          <p className="mt-1 text-[11px] font-medium text-rose-600">
                            {transfer.failureMessage}
                          </p>
                        )}
                      </div>

                      <div className="shrink-0 text-right">
                        <span
                          className={`font-mono text-sm font-black md:text-base ${
                            incoming ? 'text-emerald-600' : 'text-rose-600'
                          }`}
                        >
                          {incoming ? '+' : '-'}
                          {formatMoney(transfer.amountCents)}
                        </span>
                        {!incoming && transfer.feeCents > 0 && (
                          <p className="mt-0.5 text-[10px] text-slate-400">
                            + {formatMoney(transfer.feeCents)} fee
                          </p>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </Card>

          {kycStatus && (
            <Card className="mt-6 flex items-center justify-between gap-4 p-5">
              <div className="flex items-center gap-3">
                <ShieldCheck size={18} className="text-[#4f46e5]" />
                <div>
                  <p className="text-sm font-bold text-[#0b1c30]">Identity verification</p>
                  <p className="text-[11px] text-[#777587]">{kycStatusLabel(kycStatus)}</p>
                </div>
              </div>
              <StatusBadge status={kycStatus} />
            </Card>
          )}
        </div>
      </div>

      <CreateAccountModal
        open={showCreateAccount}
        onClose={() => setShowCreateAccount(false)}
        onCreated={refreshAll}
      />

      <TransferModal
        open={showTransfer}
        onClose={() => setShowTransfer(false)}
        accounts={accounts}
        balances={balances}
        onCompleted={refreshAll}
      />
    </AppLayout>
  );
}
