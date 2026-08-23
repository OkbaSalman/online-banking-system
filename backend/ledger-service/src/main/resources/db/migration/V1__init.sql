create table if not exists ledger_chain_heads (
    user_id uuid primary key,
    head_seq bigint not null,
    head_hash varchar(64) not null,
    head_entry_id uuid
);

create table if not exists ledger_entries (
    id uuid primary key,
    user_id uuid not null,
    idempotency_key varchar(200) not null,
    type varchar(50) not null,
    description varchar(500),
    created_at_epoch_ms bigint not null,

    seq bigint not null,
    prev_hash varchar(64) not null,
    entry_hash varchar(64) not null,

    from_account_id uuid not null,
    to_account_id uuid not null,
    amount_cents bigint not null
);

create unique index if not exists ux_ledger_entries_user_idempotency
    on ledger_entries (user_id, idempotency_key);

create index if not exists ix_ledger_entries_user_created
    on ledger_entries (user_id, created_at_epoch_ms desc);

create unique index if not exists ux_ledger_entries_user_seq
    on ledger_entries (user_id, seq);

create table if not exists ledger_postings (
    id uuid primary key,
    entry_id uuid not null,
    account_id uuid not null,
    amount_cents bigint not null
);

create index if not exists ix_ledger_postings_entry
    on ledger_postings (entry_id);

create index if not exists ix_ledger_postings_account
    on ledger_postings (account_id);

create table if not exists account_balances (
    account_id uuid primary key,
    user_id uuid not null,
    available_cents bigint not null
);

create unique index if not exists ux_account_balances_user_account
    on account_balances (user_id, account_id);

create index if not exists ix_account_balances_user
    on account_balances (user_id);