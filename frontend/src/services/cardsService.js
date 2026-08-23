import { api, unwrap } from '../api/client';
import { newIdempotencyKey } from '../lib/crypto';

export const listMyCards = ({ limit = 50, offset = 0 } = {}) =>
  unwrap(api.get('/cards', { params: { limit, offset } }));

export const getCard = (cardId) => unwrap(api.get(`/cards/${cardId}`));

export const createCard = (
  fundingAccountId,
  nickname = '',
  { dailyLimitCents = 0, monthlyLimitCents = 0, perTransactionLimitCents = 0 } = {},
) =>
  unwrap(
    api.post('/cards', {
      fundingAccountId,
      idempotencyKey: newIdempotencyKey(),
      nickname,
      dailyLimitCents,
      monthlyLimitCents,
      perTransactionLimitCents,
    }),
  ).then((data) => data.card);

export const freezeCard = (cardId) => unwrap(api.post(`/cards/${cardId}/freeze`));

export const unfreezeCard = (cardId) => unwrap(api.post(`/cards/${cardId}/unfreeze`));

export const setCardLimits = (cardId, { dailyLimitCents = 0, monthlyLimitCents = 0, perTransactionLimitCents = 0 }) =>
  unwrap(
    api.patch(`/cards/${cardId}/limits`, {
      dailyLimitCents,
      monthlyLimitCents,
      perTransactionLimitCents,
    }),
  );

export const chargeCard = (cardId, { merchantAccountId, amountCents, description = '' }) =>
  unwrap(
    api.post(`/cards/${cardId}/charge`, {
      merchantAccountId,
      amountCents,
      idempotencyKey: newIdempotencyKey(),
      description,
    }),
  ).then((data) => data.charge);

export const listCardCharges = (cardId, { limit = 50, offset = 0 } = {}) =>
  unwrap(api.get(`/cards/${cardId}/charges`, { params: { limit, offset } }));
