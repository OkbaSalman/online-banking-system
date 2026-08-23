import { api, unwrap } from '../api/client';
import { newIdempotencyKey } from '../lib/crypto';

export const createTransfer = ({
  fromAccountId,
  toAccountId,
  amountCents,
  description = '',
  idempotencyKey = newIdempotencyKey(),
}) =>
  unwrap(
    api.post('/transfers', {
      fromAccountId,
      toAccountId,
      amountCents,
      idempotencyKey,
      description,
    }),
  );

export const getTransfer = (transferId) =>
  unwrap(api.get(`/transfers/${transferId}`)).then((data) => data.transfer);

export const listMyTransfers = ({ status, fromAccountId, toAccountId, limit = 50, offset = 0 } = {}) =>
  unwrap(
    api.get('/transfers', { params: { status, fromAccountId, toAccountId, limit, offset } }),
  ).then((data) => data.transfers || []);

export const adminMint = ({
  toAccountId,
  amountCents,
  description = '',
  idempotencyKey = newIdempotencyKey(),
}) =>
  unwrap(
    api.post('/transfers/admin/mint', { toAccountId, amountCents, idempotencyKey, description }),
  );

export const adminListTransfers = ({
  status,
  initiatorUserId,
  fromAccountId,
  toAccountId,
  limit = 50,
  offset = 0,
} = {}) =>
  unwrap(
    api.get('/transfers/admin', {
      params: { status, initiatorUserId, fromAccountId, toAccountId, limit, offset },
    }),
  ).then((data) => data.transfers || []);

export const adminGetRevenueSummary = ({ year, month } = {}) =>
  unwrap(api.get('/transfers/admin/revenue', { params: { year, month } }));
