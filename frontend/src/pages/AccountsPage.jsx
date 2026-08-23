import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  BadgeCheck,
  Copy,
  Download,
  FileSearch,
  Landmark,
  Pencil,
  Plus,
  Search,
  Snowflake,
  Trash2,
  UserPlus,
  Users,
} from 'lucide-react';
import AppLayout from '../components/layout/AppLayout';
import CreateAccountModal from '../components/modals/CreateAccountModal';
import {
  Badge,
  Button,
  Card,
  DataRow,
  EmptyState,
  ErrorNotice,
  Modal,
  SectionTitle,
  SelectField,
  Spinner,
  TextField,
} from '../components/ui';
import { useAccounts, describeAccount } from '../hooks/useAccounts';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import * as accountsService from '../services/accountsService';
import * as ledgerService from '../services/ledgerService';
import { searchUsers } from '../services/usersService';
import {
  accountTypeLabel,
  formatDateTime,
  formatMoney,
  formatSignedMoney,
  invitationStatusLabel,
  isSavings,
  membershipRoleLabel,
  shortId,
} from '../lib/format';

function toCsv(rows) {
  const header = ['Date', 'Description', 'Amount', 'Counterparty account', 'Sequence', 'Entry id'];
  const body = rows.map((item) => [
    new Date(item.createdAtEpochMs).toISOString(),
    (item.entry?.description || '').replace(/"/g, '""'),
    (item.amountCents / 100).toFixed(2),
    item.counterpartyAccountId || '',
    item.seq,
    item.entryId,
  ]);
  return [header, ...body].map((row) => row.map((cell) => `"${cell}"`).join(',')).join('\n');
}

function InviteMemberForm({ accountId, onInvited }) {
  const toast = useToast();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [selected, setSelected] = useState(null);
  const [role, setRole] = useState('MEMBER');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (selected || query.trim().length < 2) {
      setResults([]);
      return undefined;
    }
    const timer = setTimeout(async () => {
      setSearching(true);
      try {
        setResults(await searchUsers(query.trim(), { limit: 6 }));
      } catch {
        setResults([]);
      } finally {
        setSearching(false);
      }
    }, 350);
    return () => clearTimeout(timer);
  }, [query, selected]);

  const handleInvite = async (event) => {
    event.preventDefault();
    setError(null);
    if (!selected) return setError('Search for the person you want to invite and select them.');

    setSubmitting(true);
    try {
      await accountsService.inviteMember(accountId, selected.userId, role);
      toast.success('Invitation sent', `${selected.email} can now accept from their dashboard.`);
      setSelected(null);
      setQuery('');
      await onInvited?.();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleInvite} className="space-y-3 border-t border-slate-100 pt-4">
      <ErrorNotice>{error}</ErrorNotice>

      {selected ? (
        <div className="flex items-center justify-between rounded-xl border border-[#3525cd]/20 bg-[#eff4ff] p-3">
          <div className="min-w-0">
            <p className="truncate text-xs font-bold text-[#0b1c30]">{selected.email}</p>
            <p className="break-all font-mono text-[10px] text-slate-500">{selected.userId}</p>
          </div>
          <button
            type="button"
            onClick={() => setSelected(null)}
            className="cursor-pointer text-[10px] font-bold uppercase text-[#3525cd] hover:underline"
          >
            Change
          </button>
        </div>
      ) : (
        <div className="relative">
          <TextField
            label="Invite a co-owner"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search by email"
            hint={searching ? 'Searching…' : 'Type at least two characters.'}
          />
          {results.length > 0 && (
            <div className="mt-2 max-h-44 divide-y divide-slate-100 overflow-y-auto rounded-xl border border-slate-100 bg-white shadow-sm">
              {results.map((user) => (
                <button
                  key={user.userId}
                  type="button"
                  onClick={() => {
                    setSelected(user);
                    setResults([]);
                  }}
                  className="flex w-full cursor-pointer items-center justify-between gap-2 p-3 text-left transition-colors hover:bg-[#eff4ff]"
                >
                  <span className="truncate text-xs font-semibold text-[#0b1c30]">{user.email}</span>
                  {user.blocked && <Badge tone="rose">Blocked</Badge>}
                </button>
              ))}
            </div>
          )}
        </div>
      )}

      <SelectField label="Role" value={role} onChange={(event) => setRole(event.target.value)}>
        <option value="MEMBER">Member — can view and spend</option>
        <option value="OWNER">Owner — full control</option>
      </SelectField>

      <Button type="submit" size="lg" loading={submitting} icon={UserPlus}>
        Send invitation
      </Button>
    </form>
  );
}

export default function AccountsPage() {
  const { user } = useAuth();
  const toast = useToast();
  const [searchParams, setSearchParams] = useSearchParams();
  const { accounts, balances, loading: accountsLoading, reload } = useAccounts();

  const [selectedId, setSelectedId] = useState('');
  const [members, setMembers] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [entries, setEntries] = useState([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState(null);
  const [membersNote, setMembersNote] = useState(null);

  const [filter, setFilter] = useState('');
  const [entryDetail, setEntryDetail] = useState(null);
  const [entryLoading, setEntryLoading] = useState(false);

  const [showCreate, setShowCreate] = useState(false);
  const [renaming, setRenaming] = useState(false);
  const [nickname, setNickname] = useState('');

  const selectedAccount = useMemo(
    () => accounts.find((account) => account.id === selectedId) || null,
    [accounts, selectedId],
  );

  const isAccountOwner = useMemo(
    () =>
      members.some(
        (member) =>
          member.userId === user?.userId && String(member.role || '').includes('OWNER'),
      ),
    [members, user?.userId],
  );

  useEffect(() => {
    if (accountsLoading || !accounts.length) return;
    const requested = searchParams.get('account');
    const exists = accounts.some((account) => account.id === requested);
    if (!selectedId || !accounts.some((account) => account.id === selectedId)) {
      setSelectedId(exists ? requested : accounts[0].id);
    }
  }, [accounts, accountsLoading, searchParams, selectedId]);

  const loadDetail = useCallback(async (accountId) => {
    if (!accountId) return;
    setDetailLoading(true);
    setDetailError(null);
    setMembersNote(null);

    try {
      const [membersResult, entriesResult, invitationList] = await Promise.all([
        accountsService.listMembers(accountId).then(
          (list) => ({ ok: true, list }),
          (err) => ({ ok: false, err }),
        ),
        ledgerService.listAccountEntries(accountId, { limit: 100 }),
        accountsService.listAccountInvitations(accountId).catch(() => []),
      ]);

      setEntries(entriesResult);
      setInvitations(invitationList);

      if (membersResult.ok) {
        setMembers(membersResult.list || []);
      } else {
        setMembers([]);
        const message = String(membersResult.err?.message || '');
        if (/OWNER role required/i.test(message)) {
          setMembersNote('Only account owners can manage invitations. You can still use this account normally.');
        } else if (/Not a member/i.test(message)) {
          setDetailError(message);
        } else {
          setMembersNote('Member list is unavailable for this account right now.');
        }
      }
    } catch (err) {
      setDetailError(err.message);
      setMembers([]);
      setEntries([]);
      setInvitations([]);
    } finally {
      setDetailLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDetail(selectedId);
  }, [selectedId, loadDetail]);

  const selectAccount = (accountId) => {
    setSelectedId(accountId);
    setSearchParams(accountId ? { account: accountId } : {}, { replace: true });
  };

  const openEntry = async (entryId) => {
    setEntryLoading(true);
    setEntryDetail({ id: entryId });
    try {
      setEntryDetail(await ledgerService.getEntry(entryId));
    } catch (err) {
      toast.error('Could not load the ledger entry', err.message);
      setEntryDetail(null);
    } finally {
      setEntryLoading(false);
    }
  };

  const handleExport = () => {
    const blob = new Blob([toCsv(filteredEntries)], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `statement-${selectedAccount?.iban || selectedId}.csv`;
    link.click();
    URL.revokeObjectURL(url);
    toast.success('Statement exported', `${filteredEntries.length} rows written to CSV.`);
  };

  const copyAccountId = async () => {
    await navigator.clipboard?.writeText(selectedId);
    toast.success('Account id copied', 'Share it with whoever needs to send you money.');
  };

  const removeMember = async (memberUserId) => {
    try {
      await accountsService.removeMember(selectedId, memberUserId);
      toast.success('Member removed');
      await loadDetail(selectedId);
    } catch (err) {
      toast.error('Could not remove the member', err.message);
    }
  };

  const cancelInvitation = async (invitationId) => {
    try {
      await accountsService.cancelInvitation(invitationId);
      toast.info('Invitation cancelled');
      await loadDetail(selectedId);
    } catch (err) {
      toast.error('Could not cancel the invitation', err.message);
    }
  };

  const saveNickname = async () => {
    try {
      await accountsService.setAccountDisplayName(selectedId, nickname);
      setRenaming(false);
      await reload();
      toast.success('Account name saved', 'Visible everywhere you use this account.');
    } catch (err) {
      toast.error('Could not save the account name', err.message);
    }
  };

  const filteredEntries = useMemo(() => {
    const needle = filter.trim().toLowerCase();
    if (!needle) return entries;
    return entries.filter((item) =>
      [item.entry?.description, item.counterpartyAccountId, item.entryId]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(needle)),
    );
  }, [entries, filter]);

  return (
    <AppLayout
      eyebrow="Accounts"
      title="Accounts & statements"
      description="Balances, co-owners and the full double-entry statement for each account."
      actions={
        <Button variant="secondary" icon={Plus} onClick={() => setShowCreate(true)}>
          Open account
        </Button>
      }
    >
      {accountsLoading ? (
        <Card>
          <Spinner label="Loading accounts…" />
        </Card>
      ) : accounts.length === 0 ? (
        <EmptyState
          icon={Landmark}
          title="You have no accounts yet"
          description="Open your first account to start receiving and sending money."
          action={
            <Button size="sm" icon={Plus} onClick={() => setShowCreate(true)}>
              Open an account
            </Button>
          }
        />
      ) : (
        <div className="space-y-8">
          <div className="flex flex-wrap gap-3">
            {accounts.map((account) => {
              const active = account.id === selectedId;
              return (
                <button
                  key={account.id}
                  type="button"
                  onClick={() => selectAccount(account.id)}
                  className={`cursor-pointer rounded-2xl border px-4 py-3 text-left transition-all ${
                    active
                      ? 'border-[#3525cd] bg-white shadow-[0_12px_28px_rgba(53,37,205,0.14)]'
                      : 'border-slate-200 bg-white/60 hover:border-slate-300'
                  }`}
                >
                  <div className="flex items-center gap-2">
                    <Badge tone={isSavings(account) ? 'amber' : 'indigo'}>
                      {accountTypeLabel(account.accountType)}
                    </Badge>
                    {account.frozen && <Badge tone="rose">Frozen</Badge>}
                  </div>
                  <p className="mt-1.5 text-sm font-bold text-[#0b1c30]">{describeAccount(account)}</p>
                  <p className="text-xs font-semibold text-[#464555]">
                    {balances[account.id] === null ? '—' : formatMoney(balances[account.id])}
                  </p>
                </button>
              );
            })}
          </div>

          {detailError && <ErrorNotice>{detailError}</ErrorNotice>}

          <section className="grid grid-cols-1 items-start gap-8 lg:grid-cols-12">
            <div className="relative overflow-hidden rounded-[2rem] bg-gradient-to-br from-[#1e1363] to-[#0b1c30] p-6 text-white shadow-xl sm:p-8 lg:col-span-8">
              <div className="pointer-events-none absolute right-0 top-0 h-80 w-80 rounded-full bg-indigo-500/10 blur-3xl" />

              <div className="relative flex flex-wrap items-start justify-between gap-4">
                <div className="min-w-0">
                  <span className="rounded-full bg-white/10 px-2.5 py-1 text-[9px] font-extrabold uppercase tracking-wider text-[#a59bff]">
                    {accountTypeLabel(selectedAccount?.accountType)} account
                  </span>
                  <div className="mt-3 flex items-center gap-2">
                    <h4 className="truncate text-xl font-bold text-white">
                      {selectedAccount ? describeAccount(selectedAccount) : '—'}
                    </h4>
                    <button
                      type="button"
                      onClick={() => {
                        setNickname(describeAccount(selectedAccount));
                        setRenaming(true);
                      }}
                      className="cursor-pointer text-slate-400 transition-colors hover:text-white"
                      aria-label="Rename account"
                    >
                      <Pencil size={14} />
                    </button>
                  </div>
                  <p className="mt-1 select-all font-mono text-[11px] text-slate-400">
                    {selectedAccount?.iban}
                  </p>
                  <button
                    type="button"
                    onClick={copyAccountId}
                    className="mt-2 flex cursor-pointer items-center gap-1.5 text-[10px] font-bold uppercase tracking-wider text-[#a59bff] hover:text-white"
                  >
                    <Copy size={12} /> Copy account id
                  </button>
                </div>

                {selectedAccount?.frozen && (
                  <span className="flex items-center gap-1.5 rounded-lg border border-rose-500/30 bg-rose-500/20 px-3 py-1.5 text-xs font-bold uppercase text-rose-300">
                    <Snowflake size={12} /> Frozen
                  </span>
                )}
              </div>

              <hr className="my-6 border-white/5" />

              <div className="flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-end">
                <div>
                  <p className="font-mono text-[10px] uppercase tracking-widest text-slate-400">
                    Available balance
                  </p>
                  <h3 className="mt-2 text-4xl font-black tracking-tighter sm:text-5xl">
                    {balances[selectedId] === null || balances[selectedId] === undefined
                      ? '—'
                      : formatMoney(balances[selectedId])}
                  </h3>
                  <p className="mt-2 text-[11px] text-slate-400">
                    Opened {formatDateTime(selectedAccount?.createdAtEpochMs)}
                  </p>
                </div>

                <div className="flex flex-wrap gap-2">
                  <Button variant="secondary" size="sm" icon={Download} onClick={handleExport}>
                    Export CSV
                  </Button>
                </div>
              </div>
            </div>

            <Card className="p-6 lg:col-span-4">
              <SectionTitle icon={Users} title={`Co-owners (${members.length})`} />

              {detailLoading ? (
                <Spinner />
              ) : (
                <>
                  {membersNote && (
                    <p className="mb-3 rounded-xl bg-slate-50 px-3 py-2 text-[11px] text-[#777587]">
                      {membersNote}
                    </p>
                  )}
                  <div className="mb-4 max-h-52 space-y-2 overflow-y-auto pr-1">
                    {members.map((member) => {
                      const isSelf = member.userId === user?.userId;
                      const isOwner = String(member.role).includes('OWNER');
                      return (
                        <div
                          key={member.userId}
                          className="flex items-center justify-between gap-2 rounded-xl border border-slate-100 bg-slate-50 p-3"
                        >
                          <div className="min-w-0">
                            <p className="break-all text-xs font-bold text-[#0b1c30]">
                              {isSelf ? 'You' : member.userId}
                            </p>
                            <p className="text-[10px] text-slate-400">
                              Since {formatDateTime(member.createdAtEpochMs)}
                            </p>
                          </div>
                          <div className="flex shrink-0 items-center gap-1.5">
                            <Badge tone={isOwner ? 'dark' : 'indigo'}>
                              {membershipRoleLabel(member.role)}
                            </Badge>
                            {isAccountOwner && !isSelf && (
                              <button
                                type="button"
                                onClick={() => removeMember(member.userId)}
                                className="cursor-pointer rounded-md p-1 text-slate-300 transition-colors hover:bg-rose-50 hover:text-rose-600"
                                aria-label="Remove member"
                              >
                                <Trash2 size={13} />
                              </button>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>

                  {isAccountOwner && invitations.length > 0 && (
                    <div className="mb-4 space-y-2">
                      <p className="text-[10px] font-bold uppercase tracking-wider text-[#777587]">
                        Pending invitations
                      </p>
                      {invitations.map((invitation) => (
                        <div
                          key={invitation.id}
                          className="flex items-center justify-between gap-2 rounded-xl border border-amber-100 bg-amber-50/70 p-2.5"
                        >
                          <div className="min-w-0">
                            <p className="break-all font-mono text-[10px] text-amber-900">
                              {invitation.invitedUserId}
                            </p>
                            <p className="text-[10px] text-amber-700">
                              {invitationStatusLabel(invitation.status)} · expires{' '}
                              {formatDateTime(invitation.expiresAtEpochMs)}
                            </p>
                          </div>
                          <button
                            type="button"
                            onClick={() => cancelInvitation(invitation.id)}
                            className="shrink-0 cursor-pointer text-[10px] font-black uppercase text-amber-800 hover:underline"
                          >
                            Cancel
                          </button>
                        </div>
                      ))}
                    </div>
                  )}

                  {isAccountOwner ? (
                    <InviteMemberForm accountId={selectedId} onInvited={() => loadDetail(selectedId)} />
                  ) : (
                    <p className="text-[11px] text-[#777587]">
                      Only an account owner can invite or remove members.
                    </p>
                  )}
                </>
              )}
            </Card>
          </section>

          <Card className="p-6 sm:p-8">
            <SectionTitle
              icon={FileSearch}
              title="Statement"
              description={`${filteredEntries.length} of ${entries.length} ledger items`}
              action={
                <div className="relative w-full sm:w-72">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={16} />
                  <input
                    type="text"
                    value={filter}
                    onChange={(event) => setFilter(event.target.value)}
                    placeholder="Filter by reference or counterparty"
                    className="w-full rounded-xl border-0 bg-[#eff4ff] py-2.5 pl-9 pr-4 text-xs font-semibold text-[#0b1c30] transition-all focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#3525cd]/25"
                  />
                </div>
              }
            />

            {detailLoading ? (
              <Spinner label="Loading statement…" />
            ) : filteredEntries.length === 0 ? (
              <EmptyState
                icon={FileSearch}
                title={entries.length ? 'No matching entries' : 'No movements yet'}
                description={
                  entries.length
                    ? 'Try a different reference or counterparty.'
                    : 'Once money moves in or out, every posting is listed here in order.'
                }
              />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full min-w-[720px] border-collapse text-left">
                  <thead>
                    <tr className="border-b border-slate-100">
                      {['Date', 'Reference', 'Counterparty', 'Seq', 'Amount', ''].map((heading, index) => (
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
                    {filteredEntries.map((item) => {
                      const negative = item.amountCents < 0;
                      return (
                        <tr key={item.id} className="transition-colors hover:bg-slate-50/70">
                          <td className="whitespace-nowrap py-4 font-mono text-[11px] text-slate-500">
                            {formatDateTime(item.createdAtEpochMs)}
                          </td>
                          <td className="max-w-xs truncate py-4 font-bold text-[#0b1c30]">
                            {item.entry?.description || 'Ledger posting'}
                          </td>
                          <td className="whitespace-nowrap py-4 font-mono text-[11px] text-slate-400">
                            {item.counterpartyAccountId ? shortId(item.counterpartyAccountId) : 'System'}
                          </td>
                          <td className="whitespace-nowrap py-4 font-mono text-[11px] text-slate-400">
                            #{item.seq}
                          </td>
                          <td
                            className={`whitespace-nowrap py-4 text-right font-mono font-black ${
                              negative ? 'text-rose-600' : 'text-emerald-600'
                            }`}
                          >
                            {formatSignedMoney(item.amountCents)}
                          </td>
                          <td className="whitespace-nowrap py-4 text-right">
                            <button
                              type="button"
                              onClick={() => openEntry(item.entryId)}
                              className="cursor-pointer rounded-lg bg-[#eff4ff] px-3 py-1.5 text-[10px] font-black uppercase text-[#3525cd] transition-colors hover:bg-[#3525cd] hover:text-white"
                            >
                              Details
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

        </div>
      )}

      <Modal
        open={Boolean(entryDetail)}
        onClose={() => setEntryDetail(null)}
        title="Ledger entry"
        description="Both sides of the posting, exactly as recorded in ledger-service."
        size="lg"
      >
        {entryLoading ? (
          <Spinner label="Loading entry…" />
        ) : entryDetail?.postings ? (
          <div className="space-y-6">
            <div className="divide-y divide-slate-100 rounded-2xl border border-slate-100 bg-slate-50 p-4">
              <DataRow label="Entry id">
                <span className="font-mono text-[11px]">{entryDetail.id}</span>
              </DataRow>
              <DataRow label="Type">{entryDetail.type || '—'}</DataRow>
              <DataRow label="Reference">{entryDetail.description || '—'}</DataRow>
              <DataRow label="Booked">{formatDateTime(entryDetail.createdAtEpochMs)}</DataRow>
              <DataRow label="Amount">{formatMoney(entryDetail.amountCents)}</DataRow>
              <DataRow label="Idempotency key">
                <span className="font-mono text-[11px]">{entryDetail.idempotencyKey || '—'}</span>
              </DataRow>
            </div>

            <div>
              <h4 className="mb-3 text-[10px] font-black uppercase tracking-widest text-[#3525cd]">
                Postings
              </h4>
              <div className="space-y-2">
                {entryDetail.postings.map((posting, index) => (
                  <div
                    key={`${posting.accountId}-${index}`}
                    className="flex items-center justify-between rounded-xl border border-slate-100 p-3 transition-colors hover:bg-slate-50"
                  >
                    <div className="min-w-0">
                      <p className="truncate font-mono text-[11px] font-bold text-[#0b1c30]">
                        {posting.accountId}
                      </p>
                      <p className="text-[10px] text-slate-400">
                        {posting.amountCents < 0 ? 'Debit' : 'Credit'}
                      </p>
                    </div>
                    <span
                      className={`shrink-0 font-mono text-sm font-black ${
                        posting.amountCents < 0 ? 'text-rose-600' : 'text-emerald-600'
                      }`}
                    >
                      {formatSignedMoney(posting.amountCents)}
                    </span>
                  </div>
                ))}
              </div>
              <p className="mt-3 flex items-center justify-center gap-1.5 text-center text-[10px] font-semibold uppercase tracking-widest text-emerald-600">
                <BadgeCheck size={12} />
                Postings sum to zero
              </p>
            </div>
          </div>
        ) : null}
      </Modal>

      <Modal
        open={renaming}
        onClose={() => setRenaming(false)}
        title="Rename account"
        description="This name is saved on the account and shared with anyone who can see it."
        size="sm"
      >
        <div className="space-y-4">
          <TextField
            label="Account name"
            value={nickname}
            onChange={(event) => setNickname(event.target.value)}
            maxLength={80}
            autoFocus
          />
          <Button size="lg" onClick={saveNickname}>
            Save
          </Button>
        </div>
      </Modal>

      <CreateAccountModal
        open={showCreate}
        onClose={() => setShowCreate(false)}
        onCreated={async (account) => {
          await reload();
          selectAccount(account.id);
        }}
      />
    </AppLayout>
  );
}
