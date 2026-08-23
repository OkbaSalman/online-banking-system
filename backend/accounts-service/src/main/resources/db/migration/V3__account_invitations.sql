create table if not exists account_invitations (
    id uuid primary key,
    account_id uuid not null references accounts(id) on delete cascade,
    invited_user_id uuid not null,
    invited_by_user_id uuid not null,
    role varchar(32) not null,
    status varchar(32) not null,
    created_at_epoch_ms bigint not null,
    expires_at_epoch_ms bigint not null,
    responded_at_epoch_ms bigint
);

create index if not exists idx_account_invitations_invited_user_id on account_invitations(invited_user_id);
create index if not exists idx_account_invitations_status on account_invitations(status);
