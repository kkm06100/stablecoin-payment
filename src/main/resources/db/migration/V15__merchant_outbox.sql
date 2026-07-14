CREATE TABLE merchant.merchant_outbox (
  event_id         UUID PRIMARY KEY,
  merchant_id      UUID NOT NULL REFERENCES merchant.merchants(merchant_id),
  event_type       TEXT NOT NULL,
  idempotency_key  TEXT NOT NULL UNIQUE,
  status           TEXT NOT NULL,
  attempt_count    INTEGER NOT NULL DEFAULT 0,
  next_attempt_at  TIMESTAMPTZ NOT NULL,
  locked_at        TIMESTAMPTZ,
  wallet_id        UUID,
  last_error       TEXT,
  created_at       TIMESTAMPTZ NOT NULL,
  updated_at       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_merchant_outbox_pending
  ON merchant.merchant_outbox (status, next_attempt_at, created_at);
