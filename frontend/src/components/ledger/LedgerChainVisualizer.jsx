import React, { useEffect, useMemo, useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import {
  ArrowDown,
  CheckCircle2,
  Link2,
  Loader2,
  Play,
  ShieldAlert,
  ShieldCheck,
  Cuboid,
} from 'lucide-react';
import { Button, Card, EmptyState, ErrorNotice, SectionTitle, Spinner } from '../ui';
import * as ledgerService from '../../services/ledgerService';
import { formatDateTime, formatSignedMoney, shortId } from '../../lib/format';

function truncateHash(hash, size = 10) {
  if (!hash) return '—';
  const value = String(hash);
  if (value.length <= size * 2) return value;
  return `${value.slice(0, size)}…${value.slice(-size)}`;
}

function hashHue(hash) {
  if (!hash) return 220;
  let total = 0;
  for (let i = 0; i < hash.length; i += 1) total += hash.charCodeAt(i);
  return total % 360;
}

/**
 * Visual walk of the per-account hash chain from ledger-service.
 * Uses listAccountEntries (seq / prevHash / itemHash) + verify-chain.
 */
export default function LedgerChainVisualizer({
  accountId,
  title = 'Ledger hash chain',
  description = 'Each posting is a block. Its hash covers the previous block’s hash — rewrite one and the chain breaks.',
  compact = false,
}) {
  const [items, setItems] = useState([]);
  const [head, setHead] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [phase, setPhase] = useState('idle'); // idle | walking | done
  const [cursor, setCursor] = useState(-1);
  const [brokenAt, setBrokenAt] = useState(null);
  const [serverResult, setServerResult] = useState(null);

  const sorted = useMemo(
    () => [...items].sort((a, b) => Number(a.seq) - Number(b.seq)),
    [items],
  );

  const load = async () => {
    if (!accountId) return;
    setLoading(true);
    setError(null);
    setPhase('idle');
    setCursor(-1);
    setBrokenAt(null);
    setServerResult(null);

    try {
      const [entryList, chainHead] = await Promise.all([
        ledgerService.listAccountEntries(accountId, { limit: 100 }),
        ledgerService.getChainHead(accountId).catch(() => null),
      ]);
      setItems(entryList);
      setHead(chainHead);
    } catch (err) {
      setError(err.message);
      setItems([]);
      setHead(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accountId]);

  /** Local link check: block N.prevHash must equal block N-1.itemHash. */
  const localBreak = useMemo(() => {
    for (let i = 1; i < sorted.length; i += 1) {
      const previous = sorted[i - 1];
      const block = sorted[i];
      if (block.prevHash && previous.itemHash && block.prevHash !== previous.itemHash) {
        return Number(block.seq);
      }
    }
    return null;
  }, [sorted]);

  const runVerification = async () => {
    if (!sorted.length) return;
    setPhase('walking');
    setCursor(-1);
    setBrokenAt(null);
    setServerResult(null);

    for (let i = 0; i < sorted.length; i += 1) {
      setCursor(i);
      // eslint-disable-next-line no-await-in-loop
      await new Promise((resolve) => setTimeout(resolve, 280));

      if (i > 0) {
        const previous = sorted[i - 1];
        const current = sorted[i];
        if (current.prevHash && previous.itemHash && current.prevHash !== previous.itemHash) {
          setBrokenAt(Number(current.seq));
          setPhase('done');
          return;
        }
      }
    }

    try {
      const result = await ledgerService.verifyChain(accountId);
      setServerResult(result);
      if (!result.ok) setBrokenAt(Number(result.firstInvalidSeq) || null);
    } catch (err) {
      setError(err.message);
    }
    setPhase('done');
  };

  const statusTone =
    phase === 'done' && (brokenAt != null || serverResult?.ok === false)
      ? 'broken'
      : phase === 'done' && serverResult?.ok
        ? 'ok'
        : phase === 'walking'
          ? 'walking'
          : localBreak != null
            ? 'suspect'
            : 'idle';

  return (
    <Card className={compact ? 'p-5' : 'p-6 sm:p-8'}>
      <SectionTitle
        icon={Cuboid}
        title={title}
        description={description}
        action={
          <div className="flex flex-wrap gap-2">
            <Button variant="secondary" size="sm" onClick={load} disabled={loading || !accountId}>
              Reload
            </Button>
            <Button
              size="sm"
              icon={Play}
              onClick={runVerification}
              disabled={loading || !sorted.length || phase === 'walking'}
              loading={phase === 'walking'}
            >
              Verify chain
            </Button>
          </div>
        }
      />

      {error && (
        <div className="mb-4">
          <ErrorNotice>{error}</ErrorNotice>
        </div>
      )}

      {!accountId ? (
        <EmptyState
          icon={Link2}
          title="Pick an account"
          description="Choose an account to inspect its hash-linked ledger blocks."
        />
      ) : loading ? (
        <Spinner label="Loading ledger blocks…" />
      ) : sorted.length === 0 ? (
        <EmptyState
          icon={Cuboid}
          title="No blocks yet"
          description="Once money moves on this account, every posting appears here as a linked block."
        />
      ) : (
        <>
          <div
            className={`mb-6 flex flex-col gap-3 rounded-2xl border p-4 sm:flex-row sm:items-center sm:justify-between ${
              statusTone === 'ok'
                ? 'border-emerald-200 bg-emerald-50'
                : statusTone === 'broken'
                  ? 'border-rose-200 bg-rose-50'
                  : statusTone === 'walking'
                    ? 'border-indigo-200 bg-[#eff4ff]'
                    : statusTone === 'suspect'
                      ? 'border-amber-200 bg-amber-50'
                      : 'border-slate-100 bg-slate-50'
            }`}
          >
            <div className="flex items-start gap-3">
              {statusTone === 'ok' ? (
                <CheckCircle2 className="mt-0.5 shrink-0 text-emerald-600" size={20} />
              ) : statusTone === 'broken' ? (
                <ShieldAlert className="mt-0.5 shrink-0 text-rose-600" size={20} />
              ) : statusTone === 'walking' ? (
                <Loader2 className="mt-0.5 shrink-0 animate-spin text-[#3525cd]" size={20} />
              ) : (
                <ShieldCheck className="mt-0.5 shrink-0 text-[#3525cd]" size={20} />
              )}
              <div>
                <p className="text-sm font-bold text-[#0b1c30]">
                  {statusTone === 'walking'
                    ? `Checking block ${cursor + 1} of ${sorted.length}…`
                    : statusTone === 'ok'
                      ? 'Chain verified — every block hashes back to the previous one'
                      : statusTone === 'broken'
                        ? `Chain break at sequence ${brokenAt}`
                        : statusTone === 'suspect'
                          ? `Local link mismatch around sequence ${localBreak}`
                          : `${sorted.length} blocks · press Verify to walk the chain`}
                </p>
                <p className="mt-0.5 text-[11px] text-[#464555]">
                  {head
                    ? `Head seq #${head.headSeq} · head hash ${truncateHash(head.headHash, 8)}`
                    : 'Head not available'}
                  {serverResult?.message ? ` · ${serverResult.message}` : ''}
                </p>
              </div>
            </div>
            <div className="flex gap-4 text-[11px] font-semibold text-[#464555]">
              <span>
                Blocks <strong className="text-[#0b1c30]">{sorted.length}</strong>
              </span>
              <span className="hidden sm:inline">
                Latest{' '}
                <strong className="font-mono text-[#0b1c30]">
                  {truncateHash(sorted[sorted.length - 1]?.itemHash, 6)}
                </strong>
              </span>
            </div>
          </div>

          <div className="relative mx-auto max-w-2xl space-y-0">
            {sorted.map((block, index) => {
              const isActive = cursor === index && phase !== 'idle';
              const isChecked = phase !== 'idle' && cursor > index;
              const isBroken = brokenAt != null && Number(block.seq) === Number(brokenAt);
              const previous = index > 0 ? sorted[index - 1] : null;
              const linkOk =
                !previous ||
                !block.prevHash ||
                !previous.itemHash ||
                block.prevHash === previous.itemHash;
              const hue = hashHue(block.itemHash);

              return (
                <div key={block.id || block.seq} className="relative">
                  {index > 0 && (
                    <div className="flex flex-col items-center py-1">
                      <div
                        className={`h-6 w-0.5 ${
                          isBroken
                            ? 'bg-rose-400'
                            : isChecked || (phase === 'done' && linkOk)
                              ? 'bg-emerald-400'
                              : 'bg-slate-200'
                        }`}
                      />
                      <div
                        className={`flex items-center gap-1 rounded-full px-2 py-0.5 text-[9px] font-black uppercase tracking-wider ${
                          isBroken
                            ? 'bg-rose-100 text-rose-700'
                            : isChecked || (phase === 'done' && linkOk)
                              ? 'bg-emerald-100 text-emerald-700'
                              : 'bg-slate-100 text-slate-500'
                        }`}
                      >
                        <ArrowDown size={10} />
                        prev → hash
                      </div>
                      <div
                        className={`h-6 w-0.5 ${
                          isBroken
                            ? 'bg-rose-400'
                            : isChecked || (phase === 'done' && linkOk)
                              ? 'bg-emerald-400'
                              : 'bg-slate-200'
                        }`}
                      />
                      <p className="mb-1 max-w-full truncate px-2 font-mono text-[9px] text-slate-400">
                        {truncateHash(block.prevHash, 12)}
                      </p>
                    </div>
                  )}

                  <motion.div
                    layout
                    animate={{
                      scale: isActive ? 1.02 : 1,
                      boxShadow: isActive
                        ? '0 18px 40px rgba(53,37,205,0.18)'
                        : '0 8px 24px rgba(11,28,48,0.05)',
                    }}
                    className={`relative overflow-hidden rounded-2xl border p-4 transition-colors ${
                      isBroken
                        ? 'border-rose-300 bg-rose-50/80'
                        : isActive
                          ? 'border-[#3525cd] bg-white'
                          : isChecked
                            ? 'border-emerald-200 bg-white'
                            : 'border-slate-100 bg-white'
                    }`}
                  >
                    <div
                      className="absolute inset-y-0 left-0 w-1.5"
                      style={{ background: `hsl(${hue} 65% 55%)` }}
                    />

                    <div className="flex flex-wrap items-start justify-between gap-3 pl-2">
                      <div className="min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="rounded-md bg-[#0b1c30] px-2 py-0.5 font-mono text-[10px] font-black text-white">
                            BLOCK #{block.seq}
                          </span>
                          {index === 0 && (
                            <span className="rounded-md bg-[#eff4ff] px-2 py-0.5 text-[9px] font-black uppercase tracking-wider text-[#3525cd]">
                              Genesis
                            </span>
                          )}
                          {index === sorted.length - 1 && (
                            <span className="rounded-md bg-amber-50 px-2 py-0.5 text-[9px] font-black uppercase tracking-wider text-amber-700">
                              Head tip
                            </span>
                          )}
                          <AnimatePresence>
                            {isChecked && !isBroken && (
                              <motion.span
                                initial={{ opacity: 0, scale: 0.8 }}
                                animate={{ opacity: 1, scale: 1 }}
                                className="inline-flex items-center gap-1 text-[10px] font-bold text-emerald-600"
                              >
                                <CheckCircle2 size={12} /> linked
                              </motion.span>
                            )}
                            {isBroken && (
                              <motion.span
                                initial={{ opacity: 0, scale: 0.8 }}
                                animate={{ opacity: 1, scale: 1 }}
                                className="inline-flex items-center gap-1 text-[10px] font-bold text-rose-600"
                              >
                                <ShieldAlert size={12} /> hash mismatch
                              </motion.span>
                            )}
                          </AnimatePresence>
                        </div>

                        <p className="mt-2 truncate text-sm font-bold text-[#0b1c30]">
                          {block.entry?.description || 'Ledger posting'}
                        </p>
                        <p className="mt-0.5 text-[11px] text-[#777587]">
                          {formatDateTime(block.createdAtEpochMs)}
                          {block.counterpartyAccountId
                            ? ` · counterparty ${shortId(block.counterpartyAccountId)}`
                            : ''}
                        </p>
                      </div>

                      <div className="shrink-0 text-right">
                        <p
                          className={`font-mono text-sm font-black ${
                            block.amountCents < 0 ? 'text-rose-600' : 'text-emerald-600'
                          }`}
                        >
                          {formatSignedMoney(block.amountCents)}
                        </p>
                        <p className="mt-1 font-mono text-[9px] text-slate-400">
                          entry {shortId(block.entryId)}
                        </p>
                      </div>
                    </div>

                    <div className="mt-3 grid grid-cols-1 gap-2 pl-2 sm:grid-cols-2">
                      <div className="rounded-xl bg-slate-50 px-3 py-2">
                        <p className="text-[9px] font-bold uppercase tracking-wider text-slate-400">
                          Item hash
                        </p>
                        <p className="mt-0.5 break-all font-mono text-[10px] font-semibold text-[#0b1c30]">
                          {truncateHash(block.itemHash, 16)}
                        </p>
                      </div>
                      <div className="rounded-xl bg-slate-50 px-3 py-2">
                        <p className="text-[9px] font-bold uppercase tracking-wider text-slate-400">
                          Previous hash
                        </p>
                        <p className="mt-0.5 break-all font-mono text-[10px] font-semibold text-[#0b1c30]">
                          {truncateHash(block.prevHash, 16)}
                        </p>
                      </div>
                    </div>
                  </motion.div>
                </div>
              );
            })}
          </div>

          <p className="mt-6 text-center text-[10px] leading-relaxed text-[#777587]">
            Verification calls <code className="font-mono text-[#3525cd]">GET /api/ledger/accounts/…/verify-chain</code>
            {' '}after walking the client-side links shown above. The server re-hashes the chain and
            reports the first invalid sequence if anything was tampered with.
          </p>
        </>
      )}
    </Card>
  );
}
