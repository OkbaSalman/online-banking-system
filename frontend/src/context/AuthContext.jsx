import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import * as authService from '../services/authService';
import {
  clearTokens,
  getAccessToken,
  getRefreshToken,
  onTokenChange,
  setTokens,
} from '../lib/tokenStore';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [status, setStatus] = useState(getAccessToken() ? 'loading' : 'anonymous');

  const loadUser = useCallback(async () => {
    try {
      const profile = await authService.getCurrentUser();
      setUser(profile);
      setStatus('authenticated');
      return profile;
    } catch (error) {
      clearTokens();
      setUser(null);
      setStatus('anonymous');
      throw error;
    }
  }, []);

  useEffect(() => {
    if (!getAccessToken()) return;
    loadUser().catch(() => {});
  }, [loadUser]);

  // The API client clears tokens when a refresh fails; mirror that in state.
  useEffect(
    () =>
      onTokenChange((hasTokens) => {
        if (!hasTokens) {
          setUser(null);
          setStatus('anonymous');
        }
      }),
    [],
  );

  const signIn = useCallback(
    async (email, password) => {
      const result = await authService.login(email, password);
      if (result.verificationRequired) return { verificationRequired: true };

      setTokens({ accessToken: result.accessToken, refreshToken: result.refreshToken });
      setStatus('loading');
      const profile = await loadUser();
      return { verificationRequired: false, user: profile };
    },
    [loadUser],
  );

  const signOut = useCallback(async () => {
    const refreshToken = getRefreshToken();
    if (refreshToken) {
      // Best effort: revoke server-side, but always clear the client session.
      await authService.logout(refreshToken).catch(() => {});
    }
    clearTokens();
    setUser(null);
    setStatus('anonymous');
  }, []);

  const value = useMemo(
    () => ({
      user,
      status,
      isAuthenticated: status === 'authenticated',
      isAdmin: user?.role === 'ADMIN',
      signIn,
      signOut,
      refreshUser: loadUser,
    }),
    [user, status, signIn, signOut, loadUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside an AuthProvider');
  return context;
}
