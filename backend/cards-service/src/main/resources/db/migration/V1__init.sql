create table if not exists cards (
    id uuid primary key,
    user_id uuid not null,
    funding_account_id uuid not null,
    last4 varchar(4) not null,
    status varchar(32) not null,
    created_at_epoch_ms bigint not null,
    idempotency_key text not null,
    nickname text,
    unique (user_id, idempotency_key)
);

create index if not exists cards_user_id_idx on cards(user_id);

create table if not exists card_charges (
    id uuid primary key,
    user_id uuid not null,
    card_id uuid not null,
    merchant_account_id uuid not null,
    amount_cents bigint not null,
    created_at_epoch_ms bigint not null,
    status varchar(32) not null,
    idempotency_key text not null,
    description text,
    transfer_id uuid,
    failure_message text,
    unique (user_id, idempotency_key)
);

create index if not exists card_charges_user_id_created_at_idx on card_charges(user_id, created_at_epoch_ms desc);
create index if not exists card_charges_card_id_created_at_idx on card_charges(card_id, created_at_epoch_ms desc);
