import { api, unwrap } from '../api/client';
import { newIdempotencyKey } from '../lib/crypto';

export const listMyAccounts = () =>
  unwrap(api.get('/accounts')).then((data) => data.accounts || []);

export const createAccount = (accountType, displayName = '') =>
  unwrap(
    api.post('/accounts', {
      idempotencyKey: newIdempotencyKey(),
      accountType,
      displayName: displayName || '',
    }),
  ).then((data) => data.account);

export const setAccountDisplayName = (accountId, displayName) =>
  unwrap(api.patch(`/accounts/${accountId}/display-name`, { displayName: displayName || '' })).then(
    (data) => data.account,
  );

export const adminListAccountsByUser = (userId) =>
  unwrap(api.get(`/accounts/admin/by-user/${userId}`)).then((data) => data.accounts || []);

export const getAccount = (accountId) =>
  unwrap(api.get(`/accounts/${accountId}`)).then((data) => data.account);

/** Returns { account, members } in one round trip. */
export const getAccountWithMembers = (accountId) => unwrap(api.get(`/accounts/${accountId}/full`));

export const listMembers = (accountId) =>
  unwrap(api.get(`/accounts/${accountId}/members`)).then((data) => data.members || []);

export const addMember = (accountId, memberUserId, role = 'MEMBER') =>
  unwrap(api.post(`/accounts/${accountId}/members`, { memberUserId, role })).then(
    (data) => data.membership,
  );

export const removeMember = (accountId, userId) =>
  unwrap(api.delete(`/accounts/${accountId}/members/${userId}`));

export const canDebit = (accountId, userId) =>
  unwrap(api.get(`/accounts/${accountId}/can-debit`, { params: { userId } }));

export const inviteMember = (accountId, invitedUserId, role = 'MEMBER', ttlSeconds = 604800) =>
  unwrap(api.post(`/accounts/${accountId}/invitations`, { invitedUserId, role, ttlSeconds }));

export const listAccountInvitations = (accountId, status = 'INVITATION_STATUS_PENDING') =>
  unwrap(api.get(`/accounts/${accountId}/invitations`, { params: { status } })).then(
    (data) => data.invitations || [],
  );

/** Invitations addressed to the signed-in user. */
export const listMyInvitations = (status = 'INVITATION_STATUS_PENDING') =>
  unwrap(api.get('/accounts/invitations', { params: { status } })).then(
    (data) => data.invitations || [],
  );

export const cancelInvitation = (invitationId) =>
  unwrap(api.delete(`/accounts/invitations/${invitationId}`));

export const acceptInvitation = (invitationId) =>
  unwrap(api.post(`/accounts/invitations/${invitationId}/accept`));

export const declineInvitation = (invitationId) =>
  unwrap(api.post(`/accounts/invitations/${invitationId}/decline`));

export const adminListAccountsByType = (accountType, { limit = 50, offset = 0 } = {}) =>
  unwrap(api.get('/accounts/admin/by-type', { params: { accountType, limit, offset } })).then(
    (data) => data.accounts || [],
  );

export const adminSetAccountFrozen = (accountId, frozen) =>
  unwrap(api.post(`/accounts/admin/${accountId}/frozen`, { frozen }));
