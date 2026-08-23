create table if not exists kyc_applications (
  id uuid primary key,
  user_id uuid not null unique,

  status varchar(32) not null,

  full_name varchar(200) not null,
  national_id varchar(100) not null,
  address varchar(500) not null,

  reviewer_user_id uuid null,
  rejection_reason varchar(500) null,

  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_kyc_applications_status on kyc_applications(status);