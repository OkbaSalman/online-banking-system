import { api, unwrap } from '../api/client';
import { newIdempotencyKey } from '../lib/crypto';

export const payBill = ({ fromAccountId, merchantAccountId, amountCents, description = '' }) =>
  unwrap(
    api.post('/billing/pay-bill', {
      fromAccountId,
      merchantAccountId,
      amountCents,
      idempotencyKey: newIdempotencyKey(),
      description,
    }),
  ).then((data) => data.payment);

export const listPayments = ({ limit = 50, offset = 0 } = {}) =>
  unwrap(api.get('/billing/payments', { params: { limit, offset } }));

export const createSubscription = ({
  fromAccountId,
  merchantAccountId,
  amountCents,
  intervalUnit = 'MONTH',
  intervalCount = 1,
  startAtEpochMs = Date.now(),
  description = '',
}) =>
  unwrap(
    api.post('/billing/subscriptions', {
      fromAccountId,
      merchantAccountId,
      amountCents,
      intervalUnit,
      intervalCount,
      startAtEpochMs,
      idempotencyKey: newIdempotencyKey(),
      description,
    }),
  ).then((data) => data.subscription);

export const listSubscriptions = ({ limit = 50, offset = 0 } = {}) =>
  unwrap(api.get('/billing/subscriptions', { params: { limit, offset } }));

export const getSubscription = (subscriptionId) =>
  unwrap(api.get(`/billing/subscriptions/${subscriptionId}`));

export const cancelSubscription = (subscriptionId) =>
  unwrap(api.post(`/billing/subscriptions/${subscriptionId}/cancel`)).then(
    (data) => data.subscription,
  );
