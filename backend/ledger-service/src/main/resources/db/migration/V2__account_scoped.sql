drop table if exists ledger_postings cascade;
drop table if exists ledger_entries cascade;
drop table if exists ledger_chain_heads cascade;
drop table if exists account_balances cascade;

create table if not exists ledger_chain_heads (
    account_id uuid primary key,
    head_seq bigint not null,
    head_hash varchar(64) not null,
    head_entry_id uuid
);

create table if not exists ledger_entries (
    id uuid primary key,
    initiator_user_id uuid not null,
    idempotency_key varchar(200) not null,
    type varchar(50) not null,
    description varchar(500),
    created_at_epoch_ms bigint not null,

    from_account_id uuid not null,
    to_account_id uuid not null,
    amount_cents bigint not null
);

create unique index if not exists ux_ledger_entries_initiator_idempotency
    on ledger_entries (initiator_user_id, idempotency_key);

create index if not exists ix_ledger_entries_created
    on ledger_entries (created_at_epoch_ms desc);

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

create table if not exists account_ledger_items (
    id uuid primary key,
    account_id uuid not null,
    entry_id uuid not null,
    created_at_epoch_ms bigint not null,
    amount_cents bigint not null,
    counterparty_account_id uuid not null,
    seq bigint not null,
    prev_hash varchar(64) not null,
    item_hash varchar(64) not null
);

create unique index if not exists ux_account_ledger_items_account_seq
    on account_ledger_items (account_id, seq);

create index if not exists ix_account_ledger_items_account_created
    on account_ledger_items (account_id, created_at_epoch_ms desc);

create index if not exists ix_account_ledger_items_entry
    on account_ledger_items (entry_id);

create table if not exists account_balances (
    account_id uuid primary key,
    available_cents bigint not null
);
