const trimTrailingSlash = (value) => value.replace(/\/+$/, '');

export const BRAND = {
  name: 'Online Bank',
  short: 'Online Bank',
  tagline: 'Online Banking',
};

export const API_BASE_URL = trimTrailingSlash(
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
);

/**
 * Browser-reachable MinIO base (scheme + host + port).
 * kyc-service often returns Docker-internal hosts like http://minio:9000 in
 * presigned URLs; the frontend rewrites those to this public base.
 */
export const MINIO_PUBLIC_BASE = trimTrailingSlash(
  import.meta.env.VITE_MINIO_PUBLIC_BASE || 'http://localhost:9000',
);

export const STORAGE_KEYS = {
  accessToken: 'onlinebank.accessToken',
  refreshToken: 'onlinebank.refreshToken',
};

/**
 * Mirrors transfers.fee.bps in transfers-service. Used for the client-side fee
 * preview only; the authoritative fee is calculated by the backend.
 */
export const TRANSFER_FEE_BPS = Number(import.meta.env.VITE_TRANSFER_FEE_BPS ?? 25);

/** Mirrors transfers.savings.max-debits-per-month in transfers-service. */
export const SAVINGS_MAX_DEBITS_PER_MONTH = Number(
  import.meta.env.VITE_SAVINGS_MAX_DEBITS_PER_MONTH ?? 10,
);

export const SYSTEM_ACCOUNTS = {
  treasury: '00000000-0000-0000-0000-000000000001',
  revenue: '00000000-0000-0000-0000-000000000002',
};

/**
 * Swap Docker-only MinIO hosts (minio, minio:9000, …) for the public base so
 * a frontend running on your laptop can reach the server.
 */
export function rewriteMinioUrl(url) {
  if (!url || !MINIO_PUBLIC_BASE) return url;
  try {
    const original = new URL(url);
    const isInternalMinio =
      original.hostname === 'minio' ||
      original.hostname.endsWith('.minio') ||
      original.hostname === 'localhost';

    if (!isInternalMinio) return url;

    const publicBase = new URL(MINIO_PUBLIC_BASE);
    original.protocol = publicBase.protocol;
    original.hostname = publicBase.hostname;
    original.port = publicBase.port;
    return original.toString();
  } catch {
    return url;
  }
}
