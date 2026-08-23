create table if not exists accounts (
    id uuid primary key,
    iban varchar(34) not null unique,
    created_at_epoch_ms bigint not null,
    created_by_user_id uuid not null,
    idempotency_key varchar(128) not null,
    unique (created_by_user_id, idempotency_key)
);

create table if not exists account_memberships (
    account_id uuid not null references accounts(id) on delete cascade,
    user_id uuid not null,
    role varchar(32) not null,
    created_at_epoch_ms bigint not null,
    primary key (account_id, user_id)
);

create index if not exists idx_account_memberships_user_id on account_memberships(user_id);
