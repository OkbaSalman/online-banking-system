import { api, ApiError, unwrap } from '../api/client';
import { MINIO_PUBLIC_BASE, rewriteMinioUrl } from '../config';
import { sha256Hex } from '../lib/crypto';

export const DOCUMENT_TYPES = [
  { value: 'ID_FRONT', label: 'ID card — front' },
  { value: 'ID_BACK', label: 'ID card — back' },
  { value: 'PROOF_ADDRESS', label: 'Proof of address' },
  { value: 'SELFIE', label: 'Selfie holding ID' },
];

export const getMyKyc = () => unwrap(api.get('/kyc/me')).then((data) => data.application);

export const listMyDocuments = () =>
  unwrap(api.get('/kyc/documents')).then((data) => data.documents || []);

export const createDocumentSlot = ({ type, originalFilename, contentType, sizeBytes, sha256 }) =>
  unwrap(api.post('/kyc/documents/slots', { type, originalFilename, contentType, sizeBytes, sha256 }));

export const getDocumentDownloadUrl = async (documentId) => {
  const url = await unwrap(api.get(`/kyc/documents/${documentId}/download-url`)).then(
    (data) => data.downloadUrl,
  );
  return rewriteMinioUrl(url);
};

export const submitKyc = ({ fullName, nationalId, address, documentIds }) =>
  unwrap(api.post('/kyc/submit', { fullName, nationalId, address, documentIds })).then(
    (data) => data.application,
  );

export const adminListPending = ({ limit = 50, offset = 0 } = {}) =>
  unwrap(api.get('/kyc/admin/pending', { params: { limit, offset } })).then(
    (data) => data.applications || [],
  );

export const adminListApplications = ({ status, limit = 50, offset = 0 } = {}) =>
  unwrap(api.get('/kyc/admin/applications', { params: { status, limit, offset } })).then(
    (data) => data.applications || [],
  );

export const adminReview = (applicationId, approve, rejectionReason = '') =>
  unwrap(
    api.post(`/kyc/admin/applications/${applicationId}/review`, { approve, rejectionReason }),
  ).then((data) => data.application);

export const adminListDocuments = (applicationId) =>
  unwrap(api.get(`/kyc/admin/applications/${applicationId}/documents`)).then(
    (data) => data.documents || [],
  );

export const adminGetDocumentDownloadUrl = async (documentId) => {
  const url = await unwrap(api.get(`/kyc/admin/documents/${documentId}/download-url`)).then(
    (data) => data.downloadUrl,
  );
  return rewriteMinioUrl(url);
};

function describeUploadFailure(error, uploadUrl) {
  let host = 'the MinIO host';
  try {
    host = new URL(uploadUrl).host;
  } catch {
    /* keep fallback */
  }

  const isCors =
    error?.name === 'TypeError' ||
    /Failed to fetch|NetworkError|CORS|cross-origin/i.test(String(error?.message || ''));

  if (isCors) {
    return (
      `Document slot was created, but the browser could not PUT the file to MinIO at ${host}. ` +
      `Open port 9000 on the server firewall for your PC, enable MinIO CORS for http://localhost:5173, ` +
      `and confirm VITE_MINIO_PUBLIC_BASE=${MINIO_PUBLIC_BASE}.`
    );
  }

  return (
    `Document slot was created, but uploading to MinIO (${host}) failed. ` +
    `If you see HTTP 403, the URL was signed for a different host than ${MINIO_PUBLIC_BASE} — ` +
    `set MINIO_PUBLIC_ENDPOINT on the server to that same base and recreate kyc-service.`
  );
}

/**
 * Two-step upload: create a document slot, then PUT the file to the (rewritten)
 * MinIO presigned URL.
 */
export async function uploadDocument(file, type, onProgress) {
  onProgress?.('Hashing file…');
  const sha256 = await sha256Hex(file);

  onProgress?.('Reserving a document slot…');
  const { document, uploadUrl } = await createDocumentSlot({
    type,
    originalFilename: file.name,
    contentType: file.type || 'application/octet-stream',
    sizeBytes: file.size,
    sha256,
  });

  const publicUploadUrl = rewriteMinioUrl(uploadUrl);
  onProgress?.(
    publicUploadUrl !== uploadUrl
      ? `Uploading via ${MINIO_PUBLIC_BASE}…`
      : 'Uploading to secure storage…',
  );

  try {
    // Content-Type is not part of the MinIO signature here; omitting it avoids
    // an extra CORS preflight on many MinIO setups.
    const response = await fetch(publicUploadUrl, {
      method: 'PUT',
      body: file,
      mode: 'cors',
    });
    if (!response.ok) {
      throw new ApiError(
        `MinIO rejected the upload with HTTP ${response.status} at ${new URL(publicUploadUrl).host}. ` +
          (response.status === 403
            ? `The signature was probably issued for "minio:9000". On the server set MINIO_PUBLIC_ENDPOINT=${MINIO_PUBLIC_BASE} and recreate kyc-service so new URLs match.`
            : 'Check firewall, CORS, and that the bucket exists.'),
        { status: response.status },
      );
    }
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError(describeUploadFailure(error, publicUploadUrl), { cause: error });
  }

  onProgress?.('Upload complete.');
  return document;
}
