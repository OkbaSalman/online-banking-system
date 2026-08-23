import React, { useCallback, useEffect, useState } from 'react';
import {
  ArrowRightLeft,
  Ban,
  Coins,
  Cuboid,
  Download,
  Inbox,
  Landmark,
  LayoutDashboard,
  Search,
  ShieldCheck,
  Snowflake,
  Sun,
  UserCheck,
  Users,
} from 'lucide-react';
import AppLayout from '../components/layout/AppLayout';
import LedgerChainVisualizer from '../components/ledger/LedgerChainVisualizer';
import {
  Badge,
  Button,
  Card,
  DataRow,
  EmptyState,
  ErrorNotice,
  Modal,
  MoneyField,
  SectionTitle,
  SelectField,
  Spinner,
  StatusBadge,
  TextField,
} from '../components/ui';
import { useToast } from '../context/ToastContext';
import * as accountsService from '../services/accountsService';
import * as transfersService from '../services/transfersService';
import * as usersService from '../services/usersService';
import * as kycService from '../services/kycService';
import { SYSTEM_ACCOUNTS } from '../config';
import {
  accountTypeLabel,
  documentTypeLabel,
  formatDateTime,
  formatMoney,
  isUuid,
  kycStatusLabel,
  maskIban,
} from '../lib/format';

const TABS = [
  { id: 'overview', label: 'Overview', icon: LayoutDashboard },
  { id: 'treasury', label: 'Treasury', icon: Coins },
  { id: 'kyc', label: 'KYC review', icon: ShieldCheck },
  { id: 'users', label: 'Users', icon: Users },
  { id: 'accounts', label: 'Accounts', icon: Landmark },
  { id: 'transfers', label: 'Transfers', icon: ArrowRightLeft },
];

function MonoId({ value, className = '' }) {
  if (!value) return <span className="text-slate-400">—</span>;
  return (
    <span
      className={`inline-block max-w-full select-all break-all font-mono text-[11px] text-[#0b1c30] ${className}`}
      title={value}
    >
      {value}
    </span>
  );
}

function systemLabel(accountId) {
  if (accountId === SYSTEM_ACCOUNTS.treasury) return 'Treasury (mint source)';
  if (accountId === SYSTEM_ACCOUNTS.revenue) return 'Revenue (fees & interest)';
  return null;
}

const MONTH_NAMES = [
  'January',
  'February',
  'March',
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'December',
];

function utcPeriod(preset) {
  const now = new Date();
  const year = now.getUTCFullYear();
  const month = now.getUTCMonth() + 1;
  if (preset === 'this-month') return { year, month };
  if (preset === 'last-month') {
    return month === 1 ? { year: year - 1, month: 12 } : { year, month: month - 1 };
  }
  if (preset === 'last-year') return { year: year - 1 };
  return { year };
}

function TreasuryTab() {
  const toast = useToast();
  const [toAccountId, setToAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [result, setResult] = useState(null);

  const handleMint = async (event) => {
    event.preventDefault();
    setError(null);

    const amountCents = Math.round(Number(amount) * 100);
    if (!isUuid(toAccountId)) return setError('Enter the destination account id (UUID).');
    if (!Number.isFinite(amountCents) || amountCents <= 0) {
      return setError('Enter an amount greater than zero.');
    }

    setSubmitting(true);
    try {
      const response = await transfersService.adminMint({
        toAccountId: toAccountId.trim(),
        amountCents,
        description: description.trim() || 'Treasury issuance',
      });
      setResult(response);
      toast.success(
        'Funds issued',
        `${formatMoney(response.transfer.amountCents)} credited. Treasury now holds ${formatMoney(response.treasuryBalanceCents)}.`,
      );
      setAmount('');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
      <Card className="p-6 sm:p-8 lg:col-span-7">
        <SectionTitle
          icon={Coins}
          title="Issue funds from treasury"
          description="Credits a customer account from the system treasury (mint)."
        />

        <form onSubmit={handleMint} className="space-y-4">
          <ErrorNotice>{error}</ErrorNotice>

          <TextField
            label="Destination account id"
            value={toAccountId}
            onChange={(event) => setToAccountId(event.target.value)}
            placeholder="00000000-0000-0000-0000-000000000000"
            className="font-mono text-xs"
            required
          />

          <MoneyField
            label="Amount"
            value={amount}
            onChange={(event) => setAmount(event.target.value)}
            placeholder="1000.00"
            required
          />

          <TextField
            label="Reference"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            placeholder="Treasury issuance"
            maxLength={140}
          />

          <Button type="submit" size="lg" loading={submitting} icon={Coins}>
            Issue funds
          </Button>
        </form>

        {result && (
          <div className="mt-6 divide-y divide-slate-100 rounded-2xl bg-slate-50 p-4">
            <DataRow label="Transfer">
              <MonoId value={result.transfer.id} />
            </DataRow>
            <DataRow label="Credited">{formatMoney(result.toBalanceCents)}</DataRow>
            <DataRow label="Treasury balance">{formatMoney(result.treasuryBalanceCents)}</DataRow>
          </div>
        )}
      </Card>

      <Card className="p-6 lg:col-span-5">
        <SectionTitle icon={Landmark} title="System accounts" />
        <div className="space-y-3">
          <div className="rounded-2xl border border-indigo-100 bg-indigo-50/60 p-4">
            <p className="text-[10px] font-bold uppercase tracking-widest text-[#3525cd]">
              Treasury · mint source
            </p>
            <p className="mt-1 select-all break-all font-mono text-[11px] text-[#0b1c30]">
              {SYSTEM_ACCOUNTS.treasury}
            </p>
            <p className="mt-2 text-[11px] text-[#777587]">
              Every admin mint debits this account and credits the destination.
            </p>
          </div>
          <div className="rounded-2xl border border-slate-100 bg-slate-50 p-4">
            <p className="text-[10px] font-bold uppercase tracking-widest text-[#777587]">
              Revenue · fees & interest
            </p>
            <p className="mt-1 select-all break-all font-mono text-[11px] text-[#0b1c30]">
              {SYSTEM_ACCOUNTS.revenue}
            </p>
            <p className="mt-2 text-[11px] text-[#777587]">
              Collects transfer fees and funds monthly savings interest.
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
}

function KycTab() {
  const toast = useToast();
  const [queue, setQueue] = useState('pending');
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [reviewing, setReviewing] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [docsLoading, setDocsLoading] = useState(false);
  const [rejectionReason, setRejectionReason] = useState('');
  const [reviewAction, setReviewAction] = useState(null);

  const pendingQueue = queue === 'pending';

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const list = pendingQueue
        ? await kycService.adminListPending()
        : await kycService.adminListApplications({ status: 'REVIEWED', limit: 50 });
      setApplications(list);
    } catch (err) {
      toast.error(
        pendingQueue ? 'Could not load pending applications' : 'Could not load completed applications',
        err.message,
      );
    } finally {
      setLoading(false);
    }
  }, [pendingQueue, toast]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (!reviewing?.id) {
      setDocuments([]);
      return undefined;
    }
    let cancelled = false;
    setDocsLoading(true);
    kycService
      .adminListDocuments(reviewing.id)
      .then((docs) => {
        if (!cancelled) setDocuments(docs);
      })
      .catch((err) => {
        if (!cancelled) {
          setDocuments([]);
          toast.error('Could not load documents', err.message);
        }
      })
      .finally(() => {
        if (!cancelled) setDocsLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [reviewing, toast]);

  const review = async (approve) => {
    setReviewAction(approve ? 'approve' : 'reject');
    try {
      await kycService.adminReview(reviewing.id, approve, approve ? '' : rejectionReason.trim());
      toast.success(approve ? 'Application approved' : 'Application rejected');
      setReviewing(null);
      setRejectionReason('');
      await load();
    } catch (err) {
      toast.error('Review failed', err.message);
    } finally {
      setReviewAction(null);
    }
  };

  const openDocument = async (documentId) => {
    try {
      const url = await kycService.adminGetDocumentDownloadUrl(documentId);
      window.open(url, '_blank', 'noopener,noreferrer');
    } catch (err) {
      toast.error('Download failed', err.message);
    }
  };

  return (
    <Card className="p-6 sm:p-8">
      <SectionTitle
        icon={ShieldCheck}
        title={pendingQueue ? `Pending applications (${applications.length})` : `Completed applications (${applications.length})`}
        description={
          pendingQueue
            ? 'Identity checks waiting on a decision. Open a review to inspect uploaded documents.'
            : 'Approved and rejected applications. Open any row to inspect the decision and documents.'
        }
        action={
          <div className="flex rounded-xl bg-slate-100 p-1">
            {[
              { id: 'pending', label: 'Pending' },
              { id: 'reviewed', label: 'Completed' },
            ].map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => setQueue(item.id)}
                className={`cursor-pointer rounded-lg px-3 py-1.5 text-[11px] font-bold ${
                  queue === item.id ? 'bg-white text-[#0b1c30] shadow-sm' : 'text-[#777587]'
                }`}
              >
                {item.label}
              </button>
            ))}
          </div>
        }
      />

      {loading ? (
        <Spinner />
      ) : applications.length === 0 ? (
        <EmptyState
          icon={UserCheck}
          title={pendingQueue ? 'Nothing to review' : 'No completed applications'}
          description={
            pendingQueue
              ? 'Every submitted application has been handled.'
              : 'Approved and rejected applications will appear here after a decision.'
          }
        />
      ) : (
        <div className="space-y-3">
          {applications.map((application) => (
            <div
              key={application.id}
              className="flex flex-col items-start justify-between gap-3 rounded-2xl border border-slate-100 bg-slate-50/70 p-4 sm:flex-row sm:items-center"
            >
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <p className="text-sm font-bold text-[#0b1c30]">{application.fullName}</p>
                  {!pendingQueue && <StatusBadge status={application.status} />}
                </div>
                <p className="mt-0.5 break-words text-[11px] text-[#464555]">{application.address}</p>
                <p className="mt-1 font-mono text-[10px] text-slate-400">
                  National id {application.nationalId} · submitted{' '}
                  {formatDateTime(application.createdAtEpochMs)}
                </p>
                {!pendingQueue && application.rejectionReason && (
                  <p className="mt-1 text-[11px] text-rose-600">{application.rejectionReason}</p>
                )}
                <p className="mt-1">
                  <MonoId value={application.userId} className="text-slate-400" />
                </p>
              </div>
              <Button size="sm" onClick={() => setReviewing(application)}>
                {pendingQueue ? 'Review' : 'Details'}
              </Button>
            </div>
          ))}
        </div>
      )}

      <Modal
        open={Boolean(reviewing)}
        onClose={() => setReviewing(null)}
        title={pendingQueue ? 'Review application' : 'Application details'}
        description={
          pendingQueue
            ? 'Inspect the uploaded documents, then approve or reject with a clear reason.'
            : `${kycStatusLabel(reviewing?.status)} on ${formatDateTime(reviewing?.updatedAtEpochMs || reviewing?.createdAtEpochMs)}.`
        }
      >
        {reviewing && (
          <div className="space-y-5">
            <div className="divide-y divide-slate-100 rounded-2xl bg-slate-50 p-4">
              <DataRow label="Status">
                <StatusBadge status={reviewing.status} />
              </DataRow>
              <DataRow label="Name">{reviewing.fullName}</DataRow>
              <DataRow label="National id">{reviewing.nationalId}</DataRow>
              <DataRow label="Address">
                <span className="break-words text-right">{reviewing.address}</span>
              </DataRow>
              <DataRow label="User">
                <MonoId value={reviewing.userId} />
              </DataRow>
              <DataRow label="Application">
                <MonoId value={reviewing.id} />
              </DataRow>
              <DataRow label="Submitted">{formatDateTime(reviewing.createdAtEpochMs)}</DataRow>
              <DataRow label="Updated">{formatDateTime(reviewing.updatedAtEpochMs)}</DataRow>
              {reviewing.reviewerUserId && (
                <DataRow label="Reviewer">
                  <MonoId value={reviewing.reviewerUserId} />
                </DataRow>
              )}
              {reviewing.rejectionReason && (
                <DataRow label="Rejection">
                  <span className="text-right text-rose-600">{reviewing.rejectionReason}</span>
                </DataRow>
              )}
            </div>

            <div>
              <p className="mb-2 text-[10px] font-bold uppercase tracking-widest text-[#777587]">
                Uploaded documents
              </p>
              {docsLoading ? (
                <Spinner />
              ) : documents.length === 0 ? (
                <p className="rounded-xl bg-amber-50 px-3 py-2 text-xs text-amber-800">
                  No documents are linked to this application.
                </p>
              ) : (
                <div className="space-y-2">
                  {documents.map((doc) => (
                    <div
                      key={doc.id}
                      className="flex items-center justify-between gap-3 rounded-xl border border-slate-100 bg-white p-3"
                    >
                      <div className="min-w-0">
                        <p className="truncate text-xs font-bold text-[#0b1c30]">
                          {doc.originalFilename || 'Document'}
                        </p>
                        <p className="text-[10px] text-slate-400">
                          {documentTypeLabel(doc.type)} · {formatDateTime(doc.uploadedAtEpochMs)}
                        </p>
                      </div>
                      <Button size="sm" variant="secondary" icon={Download} onClick={() => openDocument(doc.id)}>
                        Open
                      </Button>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {pendingQueue && (
              <>
                <TextField
                  label="Rejection reason (required to reject)"
                  value={rejectionReason}
                  onChange={(event) => setRejectionReason(event.target.value)}
                  placeholder="Document is unreadable"
                  maxLength={200}
                />

                <div className="grid grid-cols-2 gap-3">
                  <Button
                    variant="danger"
                    loading={reviewAction === 'reject'}
                    disabled={Boolean(reviewAction) || !rejectionReason.trim()}
                    onClick={() => review(false)}
                  >
                    Reject
                  </Button>
                  <Button
                    loading={reviewAction === 'approve'}
                    disabled={Boolean(reviewAction)}
                    onClick={() => review(true)}
                  >
                    Approve
                  </Button>
                </div>
              </>
            )}
          </div>
        )}
      </Modal>
    </Card>
  );
}


function UsersTab({ onOpenAccount }) {
  const toast = useToast();
  const [query, setQuery] = useState('');
  const [users, setUsers] = useState([]);
  const [searching, setSearching] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [userAccounts, setUserAccounts] = useState([]);
  const [accountsLoading, setAccountsLoading] = useState(false);

  useEffect(() => {
    if (query.trim().length < 2) {
      setUsers([]);
      return undefined;
    }
    const timer = setTimeout(async () => {
      setSearching(true);
      try {
        setUsers(await usersService.searchUsers(query.trim(), { limit: 25 }));
      } catch (err) {
        toast.error('Search failed', err.message);
      } finally {
        setSearching(false);
      }
    }, 350);
    return () => clearTimeout(timer);
  }, [query, toast]);

  const openUser = async (user) => {
    setSelectedUser(user);
    setAccountsLoading(true);
    setUserAccounts([]);
    try {
      setUserAccounts(await accountsService.adminListAccountsByUser(user.userId));
    } catch (err) {
      toast.error('Could not load accounts', err.message);
    } finally {
      setAccountsLoading(false);
    }
  };

  const toggleBlocked = async (user) => {
    try {
      await usersService.adminSetUserBlocked(user.userId, !user.blocked);
      toast.success(user.blocked ? 'User unblocked' : 'User blocked');
      const next = { ...user, blocked: !user.blocked };
      setUsers((current) =>
        current.map((item) => (item.userId === user.userId ? next : item)),
      );
      if (selectedUser?.userId === user.userId) setSelectedUser(next);
    } catch (err) {
      toast.error('Could not update the user', err.message);
    }
  };

  const toggleFrozen = async (account) => {
    try {
      await accountsService.adminSetAccountFrozen(account.id, !account.frozen);
      toast.success(account.frozen ? 'Account unfrozen' : 'Account frozen');
      setUserAccounts((current) =>
        current.map((item) =>
          item.id === account.id ? { ...item, frozen: !item.frozen } : item,
        ),
      );
    } catch (err) {
      toast.error('Could not update the account', err.message);
    }
  };

  return (
    <Card className="p-6 sm:p-8">
      <SectionTitle
        icon={Users}
        title="User directory"
        description="Find a customer, open their accounts, freeze them, or inspect the ledger."
      />

      <div className="relative mb-5">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={16} />
        <input
          type="text"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search by email"
          className="w-full rounded-xl border-0 bg-[#eff4ff] py-3.5 pl-10 pr-4 text-sm text-[#0b1c30] transition-all focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#3525cd]/25"
        />
      </div>

      {searching ? (
        <Spinner />
      ) : users.length === 0 ? (
        <EmptyState
          icon={Users}
          title={query.trim().length < 2 ? 'Search the directory' : 'No matching users'}
          description="Type at least two characters of an email address."
        />
      ) : (
        <div className="space-y-2">
          {users.map((user) => (
            <button
              key={user.userId}
              type="button"
              onClick={() => openUser(user)}
              className="flex w-full cursor-pointer flex-col items-start justify-between gap-3 rounded-xl border border-slate-100 bg-slate-50/70 p-4 text-left transition-colors hover:border-[#3525cd]/25 hover:bg-white sm:flex-row sm:items-center"
            >
              <div className="min-w-0">
                <p className="break-all text-sm font-bold text-[#0b1c30]">{user.email}</p>
                <MonoId value={user.userId} className="text-slate-400" />
              </div>
              <div className="flex shrink-0 flex-wrap items-center gap-2">
                <Badge tone={user.role === 'ADMIN' ? 'dark' : 'indigo'}>{user.role}</Badge>
                {user.emailVerified ? (
                  <Badge tone="emerald">Verified</Badge>
                ) : (
                  <Badge tone="amber">Unverified</Badge>
                )}
                {user.blocked && <Badge tone="rose">Blocked</Badge>}
              </div>
            </button>
          ))}
        </div>
      )}

      <Modal
        open={Boolean(selectedUser)}
        onClose={() => setSelectedUser(null)}
        title={selectedUser?.email || 'User'}
        description="Accounts this customer can access. Open one for the hash chain, or freeze it."
        size="2xl"
      >
        {selectedUser && (
          <div className="space-y-5">
            <div className="divide-y divide-slate-100 rounded-2xl bg-slate-50 p-4">
              <DataRow label="User id">
                <MonoId value={selectedUser.userId} />
              </DataRow>
              <DataRow label="Role">{selectedUser.role}</DataRow>
              <DataRow label="Email status">
                {selectedUser.emailVerified ? 'Verified' : 'Unverified'}
              </DataRow>
            </div>

            <Button
              size="sm"
              variant={selectedUser.blocked ? 'primary' : 'danger'}
              icon={selectedUser.blocked ? Sun : Ban}
              onClick={() => toggleBlocked(selectedUser)}
            >
              {selectedUser.blocked ? 'Unblock user' : 'Block user'}
            </Button>

            <div>
              <p className="mb-2 text-[10px] font-bold uppercase tracking-widest text-[#777587]">
                Accounts
              </p>
              {accountsLoading ? (
                <Spinner />
              ) : userAccounts.length === 0 ? (
                <EmptyState icon={Landmark} title="No accounts for this user" />
              ) : (
                <div className="space-y-2">
                  {userAccounts.map((account) => (
                    <div
                      key={account.id}
                      className="rounded-xl border border-slate-100 bg-white p-3"
                    >
                      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                        <div className="min-w-0">
                          <p className="text-xs font-bold text-[#0b1c30]">
                            {account.displayName || accountTypeLabel(account.accountType)}
                          </p>
                          <p className="font-mono text-[10px] text-slate-400">{maskIban(account.iban)}</p>
                          <MonoId value={account.id} className="text-slate-400" />
                        </div>
                        <div className="flex shrink-0 flex-wrap gap-2">
                          {account.frozen ? (
                            <Badge tone="rose">Frozen</Badge>
                          ) : (
                            <Badge tone="emerald">Active</Badge>
                          )}
                          <Button
                            size="sm"
                            variant="secondary"
                            icon={Cuboid}
                              onClick={() => onOpenAccount?.(account)}
                          >
                            Ledger
                          </Button>
                          <Button
                            size="sm"
                            variant={account.frozen ? 'primary' : 'danger'}
                            icon={account.frozen ? Sun : Snowflake}
                            onClick={() => toggleFrozen(account)}
                          >
                            {account.frozen ? 'Unfreeze' : 'Freeze'}
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </Modal>
    </Card>
  );
}

function AccountsTab() {
  const toast = useToast();
  const [accountType, setAccountType] = useState('CHECKING');
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selected, setSelected] = useState(null);
  const [lookupId, setLookupId] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setAccounts(await accountsService.adminListAccountsByType(accountType, { limit: 100 }));
    } catch (err) {
      toast.error('Could not load accounts', err.message);
    } finally {
      setLoading(false);
    }
  }, [accountType, toast]);

  useEffect(() => {
    load();
  }, [load]);

  const toggleFrozen = async (account) => {
    try {
      await accountsService.adminSetAccountFrozen(account.id, !account.frozen);
      toast.success(account.frozen ? 'Account unfrozen' : 'Account frozen');
      setAccounts((current) =>
        current.map((item) => (item.id === account.id ? { ...item, frozen: !item.frozen } : item)),
      );
      if (selected?.id === account.id) {
        setSelected({ ...account, frozen: !account.frozen });
      }
    } catch (err) {
      toast.error('Could not update the account', err.message);
    }
  };

  const openById = (event) => {
    event.preventDefault();
    if (!isUuid(lookupId)) {
      toast.error('Invalid account id', 'Paste a full UUID.');
      return;
    }
    setSelected({ id: lookupId.trim() });
  };

  const systemAccounts = [
    {
      id: SYSTEM_ACCOUNTS.treasury,
      label: 'Treasury',
      hint: 'Mint source — every issuance debits this account',
    },
    {
      id: SYSTEM_ACCOUNTS.revenue,
      label: 'Revenue',
      hint: 'Collects fees and pays savings interest',
    },
  ];

  return (
    <div className="space-y-6">
      <Card className="p-6 sm:p-8">
        <SectionTitle
          icon={Landmark}
          title="System accounts"
          description="Treasury (mint) and revenue are fixed platform accounts — open either to inspect the hash chain."
        />
        <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
          {systemAccounts.map((account) => (
            <button
              key={account.id}
              type="button"
              onClick={() => setSelected({ id: account.id, label: account.label })}
              className="cursor-pointer rounded-2xl border border-slate-100 bg-slate-50/80 p-4 text-left transition-all hover:border-[#3525cd]/30 hover:bg-white hover:shadow-sm"
            >
              <p className="text-[10px] font-bold uppercase tracking-widest text-[#3525cd]">
                {account.label}
              </p>
              <p className="mt-1 select-all break-all font-mono text-[11px] text-[#0b1c30]">
                {account.id}
              </p>
              <p className="mt-2 text-[11px] text-[#777587]">{account.hint}</p>
            </button>
          ))}
        </div>
      </Card>

      <Card className="p-6 sm:p-8">
        <SectionTitle
          icon={Cuboid}
          title="Open any account"
          description="Paste a full account id to freeze/unfreeze controls (when listed) and verify its ledger hash chain."
        />
        <form className="flex flex-col gap-3 sm:flex-row" onSubmit={openById}>
          <div className="flex-1">
            <TextField
              label="Account id"
              value={lookupId}
              onChange={(event) => setLookupId(event.target.value)}
              placeholder="00000000-0000-0000-0000-000000000000"
              className="font-mono text-xs"
            />
          </div>
          <div className="flex items-end">
            <Button type="submit" disabled={!isUuid(lookupId)} icon={Cuboid}>
              Inspect
            </Button>
          </div>
        </form>
      </Card>

      <Card className="p-6 sm:p-8">
        <SectionTitle
          icon={Landmark}
          title="Customer accounts"
          description="Browse by type. Click a row for the full id and hash chain."
          action={
            <SelectField
              value={accountType}
              onChange={(event) => setAccountType(event.target.value)}
              className="!py-2.5 text-xs"
            >
              <option value="CHECKING">Checking</option>
              <option value="SAVINGS">Savings</option>
            </SelectField>
          }
        />

        {loading ? (
          <Spinner />
        ) : accounts.length === 0 ? (
          <EmptyState icon={Landmark} title="No accounts of this type" />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] border-collapse text-left">
              <thead>
                <tr className="border-b border-slate-100">
                  {['IBAN', 'Account id', 'Type', 'Opened', 'Status', ''].map((heading, index) => (
                    <th
                      key={heading || index}
                      className="py-3 font-mono text-[10px] font-bold uppercase tracking-widest text-slate-400"
                    >
                      {heading}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50 text-xs">
                {accounts.map((account) => (
                  <tr
                    key={account.id}
                    className="cursor-pointer transition-colors hover:bg-[#eff4ff]/70"
                    onClick={() => setSelected(account)}
                  >
                    <td className="whitespace-nowrap py-4 font-mono text-[11px] font-bold text-[#0b1c30]">
                      {maskIban(account.iban)}
                    </td>
                    <td className="max-w-[18rem] py-4">
                      <MonoId value={account.id} className="text-slate-500" />
                    </td>
                    <td className="py-4">
                      <Badge tone="indigo">{accountTypeLabel(account.accountType)}</Badge>
                    </td>
                    <td className="whitespace-nowrap py-4 font-mono text-[11px] text-slate-500">
                      {formatDateTime(account.createdAtEpochMs)}
                    </td>
                    <td className="py-4">
                      {account.frozen ? (
                        <Badge tone="rose">Frozen</Badge>
                      ) : (
                        <Badge tone="emerald">Active</Badge>
                      )}
                    </td>
                    <td className="py-4 text-right">
                      <Button
                        size="sm"
                        variant={account.frozen ? 'primary' : 'danger'}
                        icon={account.frozen ? Sun : Snowflake}
                        onClick={(event) => {
                          event.stopPropagation();
                          toggleFrozen(account);
                        }}
                      >
                        {account.frozen ? 'Unfreeze' : 'Freeze'}
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      <Modal
        open={Boolean(selected)}
        onClose={() => setSelected(null)}
        title={systemLabel(selected?.id) || selected?.label || 'Account detail'}
        description="Full identifiers and the hash-linked ledger for this account."
        size="2xl"
      >
        {selected && (
          <div className="space-y-5">
            <div className="divide-y divide-slate-100 rounded-2xl bg-slate-50 p-4">
              <DataRow label="Account id">
                <MonoId value={selected.id} />
              </DataRow>
              {selected.iban && (
                <DataRow label="IBAN">
                  <span className="select-all break-all font-mono text-[11px]">{selected.iban}</span>
                </DataRow>
              )}
              {selected.accountType && (
                <DataRow label="Type">{accountTypeLabel(selected.accountType)}</DataRow>
              )}
              {selected.createdAtEpochMs != null && (
                <DataRow label="Opened">{formatDateTime(selected.createdAtEpochMs)}</DataRow>
              )}
              {typeof selected.frozen === 'boolean' && (
                <DataRow label="Status">
                  {selected.frozen ? (
                    <Badge tone="rose">Frozen</Badge>
                  ) : (
                    <Badge tone="emerald">Active</Badge>
                  )}
                </DataRow>
              )}
            </div>

            {typeof selected.frozen === 'boolean' && (
              <Button
                variant={selected.frozen ? 'primary' : 'danger'}
                icon={selected.frozen ? Sun : Snowflake}
                onClick={() => toggleFrozen(selected)}
              >
                {selected.frozen ? 'Unfreeze account' : 'Freeze account'}
              </Button>
            )}

            <LedgerChainVisualizer
              accountId={selected.id}
              title="Ledger hash chain"
              description="Blocks are ordered by sequence. Verify walks each prevHash link, then asks ledger-service to re-hash the chain."
            />
          </div>
        )}
      </Modal>
    </div>
  );
}

function AccountLedgerModal({ account, onClose, onToggleFrozen }) {
  if (!account?.id) return null;

  return (
    <Modal
      open={Boolean(account)}
      onClose={onClose}
      title={systemLabel(account.id) || account.displayName || account.label || 'Account ledger'}
      description="Close this to return where you were. Freeze stops future debits on this account."
      size="2xl"
      elevated
    >
      <div className="space-y-5">
        <div className="divide-y divide-slate-100 rounded-2xl bg-slate-50 p-4">
          <DataRow label="Account id">
            <MonoId value={account.id} />
          </DataRow>
          {account.iban && (
            <DataRow label="IBAN">
              <span className="select-all break-all font-mono text-[11px]">{account.iban}</span>
            </DataRow>
          )}
          {account.accountType && (
            <DataRow label="Type">{accountTypeLabel(account.accountType)}</DataRow>
          )}
          {typeof account.frozen === 'boolean' && (
            <DataRow label="Status">
              {account.frozen ? <Badge tone="rose">Frozen</Badge> : <Badge tone="emerald">Active</Badge>}
            </DataRow>
          )}
        </div>

        {typeof account.frozen === 'boolean' && onToggleFrozen && (
          <Button
            variant={account.frozen ? 'primary' : 'danger'}
            icon={account.frozen ? Sun : Snowflake}
            onClick={() => onToggleFrozen(account)}
          >
            {account.frozen ? 'Unfreeze account' : 'Freeze account'}
          </Button>
        )}

        <LedgerChainVisualizer
          accountId={account.id}
          title="Ledger hash chain"
          description="Blocks are ordered by sequence. Verify walks each prevHash link, then asks ledger-service to re-hash the chain."
        />
      </div>
    </Modal>
  );
}

function TransfersTab({ onOpenAccount }) {
  const toast = useToast();
  const now = new Date();
  const currentYear = now.getUTCFullYear();
  const [transfers, setTransfers] = useState([]);
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [year, setYear] = useState(currentYear);
  const [month, setMonth] = useState(now.getUTCMonth() + 1);
  const [revenue, setRevenue] = useState(null);
  const [revenueLoading, setRevenueLoading] = useState(true);

  const years = Array.from({ length: currentYear - 2019 }, (_, index) => currentYear - index);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setTransfers(await transfersService.adminListTransfers({ status: status || undefined, limit: 100 }));
    } catch (err) {
      toast.error('Could not load transfers', err.message);
    } finally {
      setLoading(false);
    }
  }, [status, toast]);

  const loadRevenue = useCallback(async () => {
    setRevenueLoading(true);
    try {
      setRevenue(
        await transfersService.adminGetRevenueSummary({
          year,
          month: month || undefined,
        }),
      );
    } catch (err) {
      toast.error('Could not load fee totals', err.message);
    } finally {
      setRevenueLoading(false);
    }
  }, [year, month, toast]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    loadRevenue();
  }, [loadRevenue]);

  const periodLabel = revenue
    ? revenue.month
      ? `${MONTH_NAMES[revenue.month - 1]} ${revenue.year}`
      : String(revenue.year)
    : 'selected period';

  return (
    <div className="space-y-6">
      <Card className="p-6 sm:p-8">
        <SectionTitle
          icon={Coins}
          title="Fee revenue"
          description={`Completed transfer fees booked in ${periodLabel} (UTC). This is what the bank collected, not the page of transfers below.`}
          action={
            <div className="flex flex-wrap items-center gap-2">
              <div className="w-28">
                <SelectField
                  value={year}
                  onChange={(event) => setYear(Number(event.target.value))}
                  className="!py-2.5 text-xs"
                >
                  {years.map((item) => (
                    <option key={item} value={item}>
                      {item}
                    </option>
                  ))}
                </SelectField>
              </div>
              <div className="w-40">
                <SelectField
                  value={month}
                  onChange={(event) => setMonth(Number(event.target.value))}
                  className="!py-2.5 text-xs"
                >
                  <option value={0}>Full year</option>
                  {MONTH_NAMES.map((name, index) => (
                    <option key={name} value={index + 1}>
                      {name}
                    </option>
                  ))}
                </SelectField>
              </div>
            </div>
          }
        />

        {revenueLoading ? (
          <Spinner />
        ) : (
          <>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              {[
                { label: 'Fees collected', value: formatMoney(revenue?.feeCents) },
                { label: 'Completed volume', value: formatMoney(revenue?.volumeCents) },
                { label: 'Completed transfers', value: revenue?.transferCount ?? 0 },
              ].map((stat) => (
                <div key={stat.label} className="rounded-2xl bg-slate-50 p-5">
                  <p className="text-[10px] font-bold uppercase tracking-widest text-[#777587]">{stat.label}</p>
                  <p className="mt-1.5 text-2xl font-black text-[#0b1c30]">{stat.value}</p>
                </div>
              ))}
            </div>
            {Number(month) === 0 && revenue?.months?.length > 0 && (
              <div className="mt-5 overflow-x-auto">
                <table className="w-full min-w-[480px] border-collapse text-left">
                  <thead>
                    <tr className="border-b border-slate-100">
                      {['Month', 'Transfers', 'Volume', 'Fees'].map((heading, index) => (
                        <th
                          key={heading}
                          className={`py-3 font-mono text-[10px] font-bold uppercase tracking-widest text-slate-400 ${
                            index >= 2 ? 'text-right' : ''
                          }`}
                        >
                          {heading}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-50 text-xs">
                    {revenue.months.map((bucket) => (
                      <tr key={`${bucket.year}-${bucket.month}`}>
                        <td className="py-3 font-bold text-[#0b1c30]">
                          {MONTH_NAMES[bucket.month - 1]} {bucket.year}
                        </td>
                        <td className="py-3 text-slate-500">{bucket.transferCount}</td>
                        <td className="py-3 text-right font-mono">{formatMoney(bucket.volumeCents)}</td>
                        <td className="py-3 text-right font-mono font-black text-[#3525cd]">
                          {formatMoney(bucket.feeCents)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </Card>

      <Card className="p-6 sm:p-8">
        <SectionTitle
          icon={ArrowRightLeft}
          title="All transfers"
          description="Read-only history. Open an account id for its ledger; freeze the account (Users/Accounts) to stop future transfers."
          action={
            <SelectField
              value={status}
              onChange={(event) => setStatus(event.target.value)}
              className="!py-2.5 text-xs"
            >
              <option value="">All statuses</option>
              <option value="COMPLETED">Completed</option>
              <option value="PENDING">Pending</option>
              <option value="BLOCKED">Blocked</option>
              <option value="FAILED">Failed</option>
            </SelectField>
          }
        />

        {loading ? (
          <Spinner />
        ) : transfers.length === 0 ? (
          <EmptyState icon={Inbox} title="No transfers found" />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[960px] border-collapse text-left">
              <thead>
                <tr className="border-b border-slate-100">
                  {['Date', 'Reference', 'From', 'To', 'Status', 'Fee', 'Amount'].map((heading, index) => (
                    <th
                      key={heading}
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
                {transfers.map((transfer) => (
                  <tr key={transfer.id} className="transition-colors hover:bg-slate-50/70">
                    <td className="whitespace-nowrap py-4 font-mono text-[11px] text-slate-500">
                      {formatDateTime(transfer.createdAtEpochMs)}
                    </td>
                    <td className="max-w-[14rem] py-4 font-bold text-[#0b1c30]">
                      <span className="break-words">{transfer.description || 'Transfer'}</span>
                    </td>
                    <td className="max-w-[14rem] py-4">
                      <button
                        type="button"
                        className="cursor-pointer text-left hover:text-[#3525cd]"
                        onClick={() => onOpenAccount({ id: transfer.fromAccountId })}
                      >
                        <MonoId value={transfer.fromAccountId} />
                        {systemLabel(transfer.fromAccountId) && (
                          <span className="mt-0.5 block text-[9px] font-bold uppercase tracking-wider text-[#3525cd]">
                            {systemLabel(transfer.fromAccountId)}
                          </span>
                        )}
                      </button>
                    </td>
                    <td className="max-w-[14rem] py-4">
                      <button
                        type="button"
                        className="cursor-pointer text-left hover:text-[#3525cd]"
                        onClick={() => onOpenAccount({ id: transfer.toAccountId })}
                      >
                        <MonoId value={transfer.toAccountId} />
                        {systemLabel(transfer.toAccountId) && (
                          <span className="mt-0.5 block text-[9px] font-bold uppercase tracking-wider text-[#3525cd]">
                            {systemLabel(transfer.toAccountId)}
                          </span>
                        )}
                      </button>
                    </td>
                    <td className="py-4">
                      <StatusBadge status={transfer.status} />
                    </td>
                    <td className="whitespace-nowrap py-4 text-right font-mono text-[11px] text-slate-400">
                      {transfer.feeCents ? formatMoney(transfer.feeCents) : '—'}
                    </td>
                    <td className="whitespace-nowrap py-4 text-right font-mono font-black text-[#0b1c30]">
                      {formatMoney(transfer.amountCents)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}

function OverviewTab({ onNavigate, pendingKyc, transferStats }) {
  const cards = [
    {
      id: 'treasury',
      title: 'Treasury mint',
      body: 'Issue sandbox funds from the system treasury into any customer account.',
      icon: Coins,
      tone: 'from-[#1e1363] to-[#0b1c30]',
    },
    {
      id: 'kyc',
      title: 'KYC queue',
      body:
        pendingKyc == null
          ? 'Approve or reject identity applications and open their documents.'
          : `${pendingKyc} application${pendingKyc === 1 ? '' : 's'} waiting on a decision.`,
      icon: ShieldCheck,
      tone: 'from-[#3525cd] to-[#1e1363]',
      badge: pendingKyc,
    },
    {
      id: 'users',
      title: 'Users',
      body: 'Search the directory and block or restore customer access.',
      icon: Users,
      tone: 'from-[#0b1c30] to-[#213145]',
    },
    {
      id: 'accounts',
      title: 'Accounts & ledger',
      body: 'Browse customer accounts, open treasury/revenue, and verify any hash chain.',
      icon: Landmark,
      tone: 'from-[#1e1363] to-[#3525cd]',
    },
    {
      id: 'transfers',
      title: 'Transfers',
      body:
        transferStats == null
          ? 'Inspect every transfer across the platform with volume and fee totals.'
          : `${formatMoney(transferStats.fees)} fees this month · ${transferStats.count} completed transfers.`,
      icon: ArrowRightLeft,
      tone: 'from-[#0b1c30] to-[#3525cd]',
    },
  ];

  return (
    <div className="space-y-8">
      <section className="relative overflow-hidden rounded-[2rem] bg-gradient-to-r from-[#0b1c30] via-[#1e1363] to-[#3525cd] p-6 text-white shadow-2xl sm:p-8">
        <div className="pointer-events-none absolute -right-10 top-0 h-72 w-72 rounded-full bg-white/10 blur-3xl" />
        <p className="font-mono text-[10px] font-bold uppercase tracking-[0.22em] text-[#a59bff]">
          Administrator workspace
        </p>
        <h3 className="mt-2 max-w-xl font-headline text-3xl font-black tracking-tight sm:text-4xl">
          Operate the bank from one console
        </h3>
        <p className="mt-3 max-w-2xl text-sm leading-relaxed text-slate-300">
          Mint funds, review identities with documents, freeze accounts, and prove every ledger
          posting still links to the one before it.
        </p>
      </section>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
        {cards.map(({ id, title, body, icon: Icon, tone, badge }) => (
          <button
            key={id}
            type="button"
            onClick={() => onNavigate(id)}
            className="group cursor-pointer overflow-hidden rounded-[1.75rem] border border-slate-100 bg-white text-left shadow-[0_10px_35px_rgba(11,28,48,0.05)] transition-all hover:-translate-y-0.5 hover:shadow-[0_18px_45px_rgba(53,37,205,0.14)]"
          >
            <div className={`relative h-24 bg-gradient-to-br ${tone} p-5`}>
              <div className="pointer-events-none absolute right-0 top-0 h-32 w-32 rounded-full bg-white/10 blur-2xl" />
              <div className="relative flex h-10 w-10 items-center justify-center rounded-xl bg-white/15 text-white">
                <Icon size={18} />
              </div>
              {badge != null && badge > 0 && (
                <span className="absolute right-4 top-4 rounded-full bg-amber-400 px-2 py-0.5 text-[10px] font-black text-[#0b1c30]">
                  {badge} pending
                </span>
              )}
            </div>
            <div className="p-5">
              <p className="text-sm font-bold text-[#0b1c30] group-hover:text-[#3525cd]">{title}</p>
              <p className="mt-1.5 text-xs leading-relaxed text-[#777587]">{body}</p>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}

export default function AdminPage() {
  const toast = useToast();
  const [tab, setTab] = useState('overview');
  const [pendingKyc, setPendingKyc] = useState(null);
  const [transferStats, setTransferStats] = useState(null);
  const [inspectAccount, setInspectAccount] = useState(null);

  useEffect(() => {
    Promise.allSettled([
      kycService.adminListPending({ limit: 50 }),
      transfersService.adminGetRevenueSummary(utcPeriod('this-month')),
    ]).then(([kycResult, revenueResult]) => {
      if (kycResult.status === 'fulfilled') setPendingKyc(kycResult.value.length);
      if (revenueResult.status === 'fulfilled') {
        const summary = revenueResult.value;
        setTransferStats({
          count: summary.transferCount || 0,
          volume: summary.volumeCents || 0,
          fees: summary.feeCents || 0,
        });
      }
    });
  }, []);

  const openAccount = (account) => {
    if (!account) return;
    const id = typeof account === 'string' ? account : account.id;
    if (!id) return;
    setInspectAccount(typeof account === 'string' ? { id: account } : account);
  };

  const toggleInspectFrozen = async (account) => {
    try {
      await accountsService.adminSetAccountFrozen(account.id, !account.frozen);
      toast.success(account.frozen ? 'Account unfrozen' : 'Account frozen');
      setInspectAccount((current) =>
        current?.id === account.id ? { ...current, frozen: !account.frozen } : current,
      );
    } catch (err) {
      toast.error('Could not update the account', err.message);
    }
  };

  return (
    <AppLayout
      eyebrow="Administration"
      title="Admin console"
      description="Treasury, compliance review, account controls, and hash-chain verification."
    >
      <div className="mb-8 flex flex-wrap gap-2">
        {TABS.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            type="button"
            onClick={() => setTab(id)}
            className={`flex cursor-pointer items-center gap-2 rounded-xl px-4 py-2.5 text-xs font-bold transition-all ${
              tab === id
                ? 'bg-[#0b1c30] text-white shadow-[0_10px_25px_rgba(11,28,48,0.18)]'
                : 'bg-white text-[#464555] hover:bg-slate-100'
            }`}
          >
            <Icon size={15} />
            {label}
            {id === 'kyc' && pendingKyc > 0 && (
              <span
                className={`rounded-full px-1.5 py-0.5 text-[9px] font-black ${
                  tab === id ? 'bg-amber-400 text-[#0b1c30]' : 'bg-amber-100 text-amber-800'
                }`}
              >
                {pendingKyc}
              </span>
            )}
          </button>
        ))}
      </div>

      {tab === 'overview' && (
        <OverviewTab onNavigate={setTab} pendingKyc={pendingKyc} transferStats={transferStats} />
      )}
      {tab === 'treasury' && <TreasuryTab />}
      {tab === 'kyc' && <KycTab />}
      {tab === 'users' && <UsersTab onOpenAccount={openAccount} />}
      {tab === 'accounts' && <AccountsTab />}
      {tab === 'transfers' && <TransfersTab onOpenAccount={openAccount} />}

      <AccountLedgerModal
        account={inspectAccount}
        onClose={() => setInspectAccount(null)}
        onToggleFrozen={
          typeof inspectAccount?.frozen === 'boolean' ? toggleInspectFrozen : undefined
        }
      />
    </AppLayout>
  );
}
