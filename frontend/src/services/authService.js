import { api, unwrap } from '../api/client';

export const register = (email, password) => unwrap(api.post('/auth/register', { email, password }));

export const verifyEmail = (email, code) => unwrap(api.post('/auth/verify-email', { email, code }));

export const resendVerification = (email) => unwrap(api.post('/auth/resend-verification', { email }));

export const login = (email, password) => unwrap(api.post('/auth/login', { email, password }));

export const refresh = (refreshToken) => unwrap(api.post('/auth/refresh', { refreshToken }));

export const logout = (refreshToken) => unwrap(api.post('/auth/logout', { refreshToken }));

export const forgotPassword = (email) => unwrap(api.post('/auth/forgot-password', { email }));

export const resetPassword = (token, newPassword) =>
  unwrap(api.post('/auth/reset-password', { token, newPassword }));

/** Returns { userId, email, role, emailVerified, blocked }. */
export const getCurrentUser = () => unwrap(api.get('/auth/me'));
