ALTER TABLE transfers
ADD COLUMN idempotency_key VARCHAR(100);

CREATE UNIQUE INDEX ux_transfers_idempotency_key
ON transfers(idempotency_key);
