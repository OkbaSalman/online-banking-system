ALTER TABLE account_invitations
    ADD COLUMN IF NOT EXISTS invited_by_email TEXT NOT NULL DEFAULT '';
