import axios from 'axios';
import { API_BASE_URL } from '../config';
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '../lib/tokenStore';

/** Paths the gateway serves without a bearer token (SecurityConfig permitAll). */
const PUBLIC_PATHS = [
  '/auth/register',
  '/auth/login',
  '/auth/verify-email',
  '/auth/refresh',
  '/auth/logout',
  '/auth/resend-verification',
  '/auth/forgot-password',
  '/auth/reset-password',
];

const isPublicPath = (url = '') => PUBLIC_PATHS.some((path) => url.startsWith(path));

export class ApiError extends Error {
  constructor(message, { status, code, cause } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.cause = cause;
  }
}

/**
 * GrpcExceptionHandler answers with RFC 7807 ProblemDetail, so `detail` holds
 * the message from the domain service and `title`/`grpcCode` the status.
 */
function toApiError(error) {
  if (axios.isCancel?.(error)) {
    return new ApiError('Request cancelled.', { code: 'CANCELLED', cause: error });
  }
  const response = error?.response;
  if (!response) {
    return new ApiError(
      'Cannot reach the banking gateway. Check that gateway-service is running and reachable.',
      { code: 'NETWORK', cause: error },
    );
  }

  const { status, data } = response;
  const detail =
    (typeof data === 'string' && data) ||
    data?.detail ||
    data?.message ||
    data?.error ||
    null;

  if (detail) {
    return new ApiError(detail, { status, code: data?.grpcCode || data?.title, cause: error });
  }

  const fallback = {
    400: 'The request was rejected as invalid.',
    401: 'Your session has expired. Please sign in again.',
    403: 'You do not have permission to perform this action.',
    404: 'The requested resource does not exist.',
    409: 'This action conflicts with the current state.',
    429: 'Too many requests. Please slow down and retry.',
    503: 'A backing service is unavailable. Please retry shortly.',
    504: 'The request timed out before the service responded.',
  }[status];

  return new ApiError(fallback || `Request failed with status ${status}.`, {
    status,
    code: data?.grpcCode,
    cause: error,
  });
}

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  if (!isPublicPath(config.url)) {
    const token = getAccessToken();
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Access tokens live 15 minutes. A single in-flight refresh is shared by every
 * request that hits a 401 so a burst of parallel calls rotates the refresh
 * token only once.
 */
let refreshPromise = null;

function refreshSession() {
  if (!refreshPromise) {
    const refreshToken = getRefreshToken();
    if (!refreshToken) return Promise.reject(new ApiError('No refresh token.', { status: 401 }));

    refreshPromise = axios
      .post(`${API_BASE_URL}/auth/refresh`, { refreshToken })
      .then(({ data }) => {
        setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken });
        return data.accessToken;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const config = error?.config;
    const status = error?.response?.status;

    const canRetry =
      status === 401 && config && !config.__isRetry && !isPublicPath(config.url) && getRefreshToken();

    if (canRetry) {
      try {
        const accessToken = await refreshSession();
        config.__isRetry = true;
        config.headers = { ...config.headers, Authorization: `Bearer ${accessToken}` };
        return await api.request(config);
      } catch {
        clearTokens();
        return Promise.reject(
          new ApiError('Your session has expired. Please sign in again.', { status: 401 }),
        );
      }
    }

    if (status === 401 && !isPublicPath(config?.url)) clearTokens();

    return Promise.reject(toApiError(error));
  },
);

export const unwrap = (promise) => promise.then((response) => response.data);
