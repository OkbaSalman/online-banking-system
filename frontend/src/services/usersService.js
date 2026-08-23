import { api, unwrap } from '../api/client';

export const searchUsers = (query, { limit = 10, offset = 0 } = {}) =>
  unwrap(api.get('/users/search', { params: { query, limit, offset } })).then(
    (data) => data.users || [],
  );

export const getUser = (userId) => unwrap(api.get(`/users/${userId}`));

export const adminGetUser = (userId) => unwrap(api.get(`/users/admin/${userId}`));

export const adminSetUserBlocked = (userId, blocked) =>
  unwrap(api.post(`/users/admin/${userId}/blocked`, { blocked }));
