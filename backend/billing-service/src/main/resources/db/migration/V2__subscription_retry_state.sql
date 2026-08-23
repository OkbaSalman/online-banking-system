ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS last_attempt_at_epoch_ms BIGINT;
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS consecutive_failures INT NOT NULL DEFAULT 0;
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS due_anchor_epoch_ms BIGINT;

UPDATE subscriptions
SET due_anchor_epoch_ms = next_charge_at_epoch_ms
WHERE due_anchor_epoch_ms IS NULL;
