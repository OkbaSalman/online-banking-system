import { STORAGE_KEYS } from '../config';

const listeners = new Set();

// One-time move from the old Meridian storage keys.
(function migrateLegacyTokens() {
  const legacyAccess = localStorage.getItem('meridian.accessToken');
  const legacyRefresh = localStorage.getItem('meridian.refreshToken');
  if (legacyAccess && !localStorage.getItem(STORAGE_KEYS.accessToken)) {
    localStorage.setItem(STORAGE_KEYS.accessToken, legacyAccess);
  }
  if (legacyRefresh && !localStorage.getItem(STORAGE_KEYS.refreshToken)) {
    localStorage.setItem(STORAGE_KEYS.refreshToken, legacyRefresh);
  }
  localStorage.removeItem('meridian.accessToken');
  localStorage.removeItem('meridian.refreshToken');
  localStorage.removeItem('meridian.accountNames');
})();

export function getAccessToken() {
  return localStorage.getItem(STORAGE_KEYS.accessToken);
}

export function getRefreshToken() {
  return localStorage.getItem(STORAGE_KEYS.refreshToken);
}

export function setTokens({ accessToken, refreshToken }) {
  if (accessToken) localStorage.setItem(STORAGE_KEYS.accessToken, accessToken);
  if (refreshToken) localStorage.setItem(STORAGE_KEYS.refreshToken, refreshToken);
  listeners.forEach((fn) => fn(true));
}

export function clearTokens() {
  localStorage.removeItem(STORAGE_KEYS.accessToken);
  localStorage.removeItem(STORAGE_KEYS.refreshToken);
  listeners.forEach((fn) => fn(false));
}

/** Lets AuthContext react when the API client force-logs-out after a failed refresh. */
export function onTokenChange(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}
