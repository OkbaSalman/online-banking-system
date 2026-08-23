alter table transfers add column if not exists fee_cents bigint not null default 0;
alter table transfers add column if not exists fee_ledger_entry_id uuid;
