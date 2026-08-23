/**
 * Every write endpoint on the gateway is idempotent on (user, idempotencyKey),
 * so each submit attempt gets a fresh key while retries of the same attempt
 * reuse it.
 */
export function newIdempotencyKey() {
  if (globalThis.crypto?.randomUUID) return globalThis.crypto.randomUUID();
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

/**
 * kyc-service stores a SHA-256 of every uploaded document so the object in
 * MinIO can be checked against what the client claimed to send.
 * crypto.subtle is only available in secure contexts (https or localhost).
 */
export async function sha256Hex(file) {
  if (!globalThis.crypto?.subtle) {
    throw new Error(
      'Document hashing requires a secure context. Open the app over HTTPS or on localhost.',
    );
  }
  const buffer = await file.arrayBuffer();
  const digest = await globalThis.crypto.subtle.digest('SHA-256', buffer);
  return Array.from(new Uint8Array(digest))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('');
}
