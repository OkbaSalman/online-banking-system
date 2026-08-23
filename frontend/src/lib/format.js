import { TRANSFER_FEE_BPS } from '../config';

const currency = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export function formatMoney(cents) {
  return currency.format((Number(cents) || 0) / 100);
}

export function formatSignedMoney(cents) {
  const value = Number(cents) || 0;
  return `${value < 0 ? '-' : '+'}${currency.format(Math.abs(value) / 100)}`;
}

export function formatDateTime(epochMs) {
  if (!epochMs) return '—';
  return new Date(Number(epochMs)).toLocaleString('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatDate(epochMs) {
  if (!epochMs) return '—';
  return new Date(Number(epochMs)).toLocaleDateString('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
}

/** Turns protobuf enum names such as ACCOUNT_TYPE_CHECKING into "Checking". */
export function humanizeEnum(value, ...prefixes) {
  if (!value) return '—';
  let out = String(value);
  for (const prefix of prefixes) {
    if (out.startsWith(prefix)) out = out.slice(prefix.length);
  }
  return out
    .toLowerCase()
    .split('_')
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

export const accountTypeLabel = (value) => humanizeEnum(value, 'ACCOUNT_TYPE_');
export const membershipRoleLabel = (value) => humanizeEnum(value, 'MEMBERSHIP_ROLE_');
export const transferStatusLabel = (value) => humanizeEnum(value, 'TRANSFER_STATUS_');
export const invitationStatusLabel = (value) => humanizeEnum(value, 'INVITATION_STATUS_');
export const kycStatusLabel = (value) => humanizeEnum(value, 'KYC_STATUS_');
export const documentTypeLabel = (value) => humanizeEnum(value, 'DOCUMENT_TYPE_');

export function isSavings(account) {
  return String(account?.accountType || '').includes('SAVINGS');
}

/** Last four characters of an IBAN, used as a short human-readable handle. */
export function ibanTail(iban) {
  if (!iban) return '????';
  return String(iban).slice(-4);
}

export function maskIban(iban) {
  if (!iban) return '—';
  const value = String(iban);
  if (value.length <= 8) return value;
  return `${value.slice(0, 4)} •••• ${value.slice(-4)}`;
}

export function shortId(id) {
  if (!id) return '—';
  const value = String(id);
  return value.length <= 12 ? value : `${value.slice(0, 8)}…${value.slice(-4)}`;
}

/**
 * Mirrors CreateTransferService.calculateFeeCents:
 * integer ceil(amount * bps / 10000) via (n + 9999) / 10000.
 */
export function calculateFeeCents(amountCents, feeBps = TRANSFER_FEE_BPS) {
  const amount = Number(amountCents) || 0;
  if (!feeBps || feeBps <= 0 || amount <= 0) return 0;
  return Math.floor((amount * feeBps + 9999) / 10000);
}

/** Parses a user-entered dollar amount into integer cents, or null when invalid. */
export function dollarsToCents(input) {
  if (input === null || input === undefined || String(input).trim() === '') return null;
  const value = Number(String(input).replace(/,/g, '').trim());
  if (!Number.isFinite(value) || value <= 0) return null;
  return Math.round(value * 100);
}

export function centsToDollarsInput(cents) {
  return ((Number(cents) || 0) / 100).toFixed(2);
}

/** Empty means unlimited (0). Returns null when the value is invalid. */
export function optionalLimitCents(input) {
  if (input === null || input === undefined || String(input).trim() === '') return 0;
  const value = Number(String(input).replace(/,/g, '').trim());
  if (!Number.isFinite(value) || value < 0) return null;
  return Math.round(value * 100);
}

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function isUuid(value) {
  return UUID_PATTERN.test(String(value || '').trim());
}

/** Derives a display name from an email address when no profile name exists. */
export function displayNameFromEmail(email) {
  if (!email) return 'there';
  const local = String(email).split('@')[0];
  return local
    .split(/[._-]+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

export function initialsFromEmail(email) {
  const name = displayNameFromEmail(email);
  return name
    .split(' ')
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('');
}
