create table if not exists subscriptions (
    id uuid primary key,
    user_id uuid not null,
    from_account_id uuid not null,
    merchant_account_id uuid not null,
    amount_cents bigint not null,
    interval_unit varchar(16) not null,
    interval_count int not null,
    next_charge_at_epoch_ms bigint not null,
    status varchar(16) not null,
    created_at_epoch_ms bigint not null,
    idempotency_key varchar(128) not null,
    description text
);

create unique index if not exists ux_subscriptions_user_idempotency
    on subscriptions(user_id, idempotency_key);

create index if not exists ix_subscriptions_user
    on subscriptions(user_id, created_at_epoch_ms desc);

create index if not exists ix_subscriptions_due
    on subscriptions(status, next_charge_at_epoch_ms);

create table if not exists billing_payments (
    id uuid primary key,
    user_id uuid not null,
    from_account_id uuid not null,
    merchant_account_id uuid not null,
    amount_cents bigint not null,
    created_at_epoch_ms bigint not null,
    status varchar(16) not null,
    idempotency_key varchar(128) not null,
    description text,
    transfer_id uuid,
    failure_message text,
    subscription_id uuid references subscriptions(id)
);

create unique index if not exists ux_billing_payments_user_idempotency
    on billing_payments(user_id, idempotency_key);

create index if not exists ix_billing_payments_user
    on billing_payments(user_id, created_at_epoch_ms desc);

create index if not exists ix_billing_payments_subscription
    on billing_payments(subscription_id, created_at_epoch_ms desc);
