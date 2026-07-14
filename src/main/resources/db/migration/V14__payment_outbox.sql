CREATE TABLE payment.payment_outbox (
  event_id              UUID PRIMARY KEY,
  payment_id            UUID NOT NULL REFERENCES payment.payments(payment_id),
  event_type            TEXT NOT NULL,
  idempotency_key       TEXT NOT NULL UNIQUE,
  payload               JSONB NOT NULL,
  status                TEXT NOT NULL,
  attempt_count         INTEGER NOT NULL DEFAULT 0,
  next_attempt_at       TIMESTAMPTZ NOT NULL,
  locked_at             TIMESTAMPTZ,
  external_transfer_id  UUID,
  last_error            TEXT,
  created_at            TIMESTAMPTZ NOT NULL,
  updated_at            TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_payment_outbox_pending
  ON payment.payment_outbox (status, next_attempt_at, created_at);

CREATE INDEX idx_payment_outbox_payment
  ON payment.payment_outbox (payment_id, created_at DESC);
