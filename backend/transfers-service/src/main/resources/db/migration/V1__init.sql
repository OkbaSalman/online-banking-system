create table if not exists transfers (
    id uuid primary key,
    initiator_user_id uuid not null,
    from_account_id uuid not null,
    to_account_id uuid not null,
    amount_cents bigint not null,
    idempotency_key varchar(128) not null,
    description varchar(512),
    created_at_epoch_ms bigint not null,
    status varchar(32) not null,
    ledger_entry_id uuid,
    failure_message varchar(512)
);

create unique index if not exists ux_transfers_idempotency
    on transfers(initiator_user_id, idempotency_key);

create index if not exists ix_transfers_created_at
    on transfers(created_at_epoch_ms desc);
