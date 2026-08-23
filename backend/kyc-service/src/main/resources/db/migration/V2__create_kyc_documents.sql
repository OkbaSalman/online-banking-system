create table if not exists kyc_documents (
  id uuid primary key,
  application_id uuid not null,
  user_id uuid not null,

  document_type varchar(32) not null,
  object_key varchar(500) not null,
  original_filename varchar(255) not null,
  content_type varchar(100) not null,
  size_bytes bigint not null,
  sha256 varchar(64) not null,

  uploaded_at timestamptz not null default now(),

  foreign key (application_id) references kyc_applications(id)
);

create index if not exists idx_kyc_documents_application_id on kyc_documents(application_id);
create index if not exists idx_kyc_documents_user_id on kyc_documents(user_id);