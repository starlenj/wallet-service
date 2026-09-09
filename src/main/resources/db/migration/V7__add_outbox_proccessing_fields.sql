ALTER TABLE outbox_events
ADD COLUMN processing_started_at TIMESTAMP NULL;

ALTER TABLE outbox_events
ADD COLUMN next_attempt_at TIMESTAMP NULL;
