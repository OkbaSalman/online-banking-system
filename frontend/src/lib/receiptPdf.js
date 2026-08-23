import { BRAND } from '../config';
import { formatDateTime, formatMoney } from './format';

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function statusLabel(status) {
  return String(status || 'Unknown')
    .replace(/^(TRANSFER_STATUS_|KYC_STATUS_|INVITATION_STATUS_|CARD_STATUS_|BILLING_PAYMENT_STATUS_|CARD_CHARGE_STATUS_)/, '')
    .replace(/_/g, ' ');
}

function statusTone(status) {
  const value = String(status || '').toUpperCase();
  if (value.includes('COMPLETED') || value.includes('APPROVED') || value.includes('ACTIVE')) return 'emerald';
  if (value.includes('PENDING')) return 'amber';
  if (value.includes('FAILED') || value.includes('REJECTED') || value.includes('BLOCKED')) return 'rose';
  if (value.includes('FROZEN') || value.includes('PAUSED')) return 'indigo';
  return 'neutral';
}

function inferKind(label, value) {
  const l = String(label).toLowerCase();
  if (l.includes('status')) return 'status';
  if (l.includes('failure')) return 'danger';
  if (l.includes('total')) return 'total';
  if (l.includes('amount') || l.includes('fee') || l.includes('purchase') || l.includes('debited')) return 'money';
  if (/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(String(value || ''))) return 'mono';
  return 'text';
}

function renderValue(kind, value) {
  const text = escapeHtml(value);
  if (kind === 'status') {
    return `<span class="badge badge-${statusTone(value)}">${escapeHtml(statusLabel(value))}</span>`;
  }
  if (kind === 'mono') return `<span class="mono">${text}</span>`;
  if (kind === 'danger') return `<span class="danger">${text}</span>`;
  if (kind === 'total') return `<span class="total">${text}</span>`;
  if (kind === 'money') return `<span class="money">${text}</span>`;
  return text;
}

function buildReceiptHtml({ title, subtitle, rows, filename, heroAmount, heroCaption }) {
  const printedAt = formatDateTime(Date.now());
  const bodyRows = rows
    .map((row) => {
      const label = Array.isArray(row) ? row[0] : row.label;
      const value = Array.isArray(row) ? row[1] : row.value;
      const kind = (Array.isArray(row) ? row[2] : row.kind) || inferKind(label, value);
      const rowClass = kind === 'total' ? ' row-total' : '';
      return `
        <div class="row${rowClass}">
          <span class="label">${escapeHtml(label)}</span>
          <span class="value">${renderValue(kind, value)}</span>
        </div>`;
    })
    .join('');

  const hero = heroAmount
    ? `<div class="hero-amount">
        <p class="hero-caption">${escapeHtml(heroCaption || 'Total')}</p>
        <p class="hero-figure">${escapeHtml(heroAmount)}</p>
      </div>`
    : '';

  return `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <title>${escapeHtml(filename)}</title>
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&family=Manrope:wght@700;800&display=swap" rel="stylesheet" />
    <style>
      :root {
        --ink: #0b1c30;
        --ink-soft: #464555;
        --ink-muted: #777587;
        --brand: #3525cd;
        --brand-bright: #4f46e5;
        --brand-pale: #a59bff;
        --surface: #f8f9ff;
        --sunken: #eff4ff;
      }
      * { box-sizing: border-box; }
      html, body {
        margin: 0;
        background: var(--surface);
        color: var(--ink);
        -webkit-print-color-adjust: exact;
        print-color-adjust: exact;
      }
      body {
        font-family: Inter, ui-sans-serif, system-ui, sans-serif;
        -webkit-font-smoothing: antialiased;
      }
      .page {
        min-height: 100vh;
        padding: 28px;
      }
      .sheet {
        max-width: 680px;
        margin: 0 auto;
      }
      .banner {
        position: relative;
        overflow: hidden;
        background: linear-gradient(120deg, #0b1c30 0%, #1e1363 52%, #3525cd 100%);
        color: #fff;
        padding: 32px 36px 28px;
        border-radius: 24px 24px 0 0;
      }
      .banner::after {
        content: '';
        position: absolute;
        right: -48px;
        top: -48px;
        width: 180px;
        height: 180px;
        border-radius: 999px;
        background: rgba(255, 255, 255, 0.1);
      }
      .brand-mark {
        font-size: 10px;
        font-weight: 800;
        letter-spacing: 0.22em;
        text-transform: uppercase;
        color: var(--brand-pale);
      }
      h1 {
        font-family: Manrope, Inter, sans-serif;
        font-size: 28px;
        font-weight: 800;
        letter-spacing: -0.03em;
        margin: 10px 0 6px;
      }
      .sub {
        margin: 0;
        color: #c7c4d8;
        font-size: 13px;
        line-height: 1.5;
        max-width: 36rem;
      }
      .hero-amount {
        margin-top: 22px;
        padding-top: 18px;
        border-top: 1px solid rgba(255, 255, 255, 0.14);
      }
      .hero-caption {
        margin: 0;
        font-size: 10px;
        font-weight: 800;
        letter-spacing: 0.18em;
        text-transform: uppercase;
        color: var(--brand-pale);
      }
      .hero-figure {
        margin: 6px 0 0;
        font-family: Manrope, Inter, sans-serif;
        font-size: 36px;
        font-weight: 800;
        letter-spacing: -0.04em;
      }
      .details {
        background: #fff;
        border-radius: 0 0 24px 24px;
      }
      .body {
        padding: 8px 28px 4px;
      }
      .panel {
        background: transparent;
        border: 0;
        border-radius: 0;
        padding: 0;
      }
      .row {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        gap: 16px;
        padding: 12px 0;
        border-bottom: 1px solid rgba(199, 196, 216, 0.35);
      }
      .row:last-child { border-bottom: 0; }
      .row-total {
        margin-top: 4px;
        padding-top: 14px;
      }
      .label {
        flex: 0 0 auto;
        font-size: 10px;
        font-weight: 800;
        letter-spacing: 0.12em;
        text-transform: uppercase;
        color: var(--ink-muted);
        padding-top: 3px;
      }
      .value {
        min-width: 0;
        text-align: right;
        font-size: 13px;
        font-weight: 600;
        color: var(--ink);
        word-break: break-word;
      }
      .money { font-variant-numeric: tabular-nums; }
      .total {
        color: var(--brand);
        font-family: Manrope, Inter, sans-serif;
        font-size: 16px;
        font-weight: 800;
      }
      .mono {
        font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
        font-size: 11px;
        font-weight: 500;
        color: var(--ink-soft);
      }
      .danger { color: #e11d48; }
      .badge {
        display: inline-flex;
        align-items: center;
        border-radius: 6px;
        padding: 3px 8px;
        font-size: 9px;
        font-weight: 800;
        letter-spacing: 0.12em;
        text-transform: uppercase;
      }
      .badge-emerald { background: #ecfdf5; color: #047857; }
      .badge-amber { background: #fef3c7; color: #92400e; }
      .badge-rose { background: #fff1f2; color: #be123c; }
      .badge-indigo { background: #eef2ff; color: #3525cd; }
      .badge-neutral { background: #f1f5f9; color: #475569; }
      .foot {
        display: flex;
        justify-content: space-between;
        gap: 16px;
        padding: 16px 28px 22px;
        color: var(--ink-muted);
        font-size: 11px;
        border-top: 1px solid #eef2ff;
      }
      .foot strong { color: var(--ink); font-weight: 700; }
      @page { size: A4; margin: 12mm; }
      @media print {
        html, body { background: #fff; }
        .page { padding: 0; min-height: auto; }
        .banner { border-radius: 24px 24px 0 0; }
        .details { border-radius: 0 0 24px 24px; }
      }
    </style>
  </head>
  <body>
    <div class="page">
      <article class="sheet">
        <header class="banner">
          <div class="brand-mark">${escapeHtml(BRAND.name)}</div>
          <h1>${escapeHtml(title || 'Receipt')}</h1>
          <p class="sub">${escapeHtml(subtitle || BRAND.tagline)}</p>
          ${hero}
        </header>
        <div class="details">
          <div class="body">
            <div class="panel">${bodyRows}</div>
          </div>
          <footer class="foot">
            <span>${escapeHtml(BRAND.tagline)}</span>
            <span>Issued <strong>${escapeHtml(printedAt)}</strong></span>
          </footer>
        </div>
      </article>
    </div>
  </body>
</html>`;
}

/**
 * Prints a receipt via a hidden iframe so the browser print dialog can save it
 * as PDF. Avoids window.open/document.write, which often yields a blank page.
 */
export function exportReceiptPdf({
  title,
  subtitle = '',
  rows = [],
  filename = 'receipt',
  heroAmount,
  heroCaption,
} = {}) {
  const html = buildReceiptHtml({ title, subtitle, rows, filename, heroAmount, heroCaption });
  const iframe = document.createElement('iframe');
  iframe.title = filename;
  iframe.setAttribute('aria-hidden', 'true');
  iframe.style.cssText =
    'position:fixed;top:0;left:0;width:830px;height:1180px;opacity:0;pointer-events:none;border:0;';
  document.body.appendChild(iframe);

  let cleaned = false;
  const cleanup = () => {
    if (cleaned) return;
    cleaned = true;
    iframe.remove();
  };

  const triggerPrint = async () => {
    const frameWindow = iframe.contentWindow;
    if (!frameWindow) {
      cleanup();
      throw new Error('Could not prepare the receipt for printing.');
    }
    try {
      await iframe.contentDocument?.fonts?.ready;
    } catch {
      /* print with fallback fonts */
    }
    frameWindow.addEventListener('afterprint', cleanup, { once: true });
    frameWindow.focus();
    window.setTimeout(() => frameWindow.print(), 80);
    window.setTimeout(cleanup, 60_000);
  };

  iframe.addEventListener(
    'load',
    () => {
      window.setTimeout(triggerPrint, 80);
    },
    { once: true },
  );
  iframe.srcdoc = html;
}

export function transferReceiptRows(transfer) {
  if (!transfer) return [];
  return [
    ['Status', transfer.status, 'status'],
    ['Amount', formatMoney(transfer.amountCents), 'money'],
    ['Fee', formatMoney(transfer.feeCents), 'money'],
    ['Total debited', formatMoney((transfer.amountCents || 0) + (transfer.feeCents || 0)), 'total'],
    ['From account', transfer.fromAccountId, 'mono'],
    ['To account', transfer.toAccountId, 'mono'],
    ['Reference', transfer.description || '—'],
    ['Booked', formatDateTime(transfer.createdAtEpochMs)],
    ['Transfer id', transfer.id, 'mono'],
    ['Ledger entry', transfer.ledgerEntryId || '—', transfer.ledgerEntryId ? 'mono' : 'text'],
    ['Fee entry', transfer.feeLedgerEntryId || '—', transfer.feeLedgerEntryId ? 'mono' : 'text'],
  ];
}

export function cardChargeReceiptRows(charge) {
  if (!charge) return [];
  return [
    ['Status', charge.status, 'status'],
    ['Purchase amount', formatMoney(charge.amountCents), 'money'],
    ['Processing fee', formatMoney(charge.feeCents), 'money'],
    ['Total debited', formatMoney((charge.amountCents || 0) + (charge.feeCents || 0)), 'total'],
    ['Merchant account', charge.merchantAccountId, 'mono'],
    ['Card', charge.cardId, 'mono'],
    ['Reference', charge.description || 'Card purchase'],
    ['Booked', formatDateTime(charge.createdAtEpochMs)],
    ['Charge id', charge.id, 'mono'],
    ['Transfer id', charge.transferId || '—', charge.transferId ? 'mono' : 'text'],
  ];
}

export function billingPaymentReceiptRows(payment) {
  if (!payment) return [];
  return [
    ['Status', payment.status, 'status'],
    ['Amount', formatMoney(payment.amountCents), 'money'],
    ['From account', payment.fromAccountId, 'mono'],
    ['Merchant account', payment.merchantAccountId, 'mono'],
    ['Reference', payment.description || 'Payment'],
    ['Booked', formatDateTime(payment.createdAtEpochMs)],
    ['Payment id', payment.id, 'mono'],
    ['Subscription', payment.subscriptionId || '—', payment.subscriptionId ? 'mono' : 'text'],
    ['Transfer id', payment.transferId || '—', payment.transferId ? 'mono' : 'text'],
  ];
}
