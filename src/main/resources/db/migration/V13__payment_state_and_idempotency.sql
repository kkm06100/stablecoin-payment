CREATE TABLE payment.idempotency_requests (
  request_id   UUID PRIMARY KEY,
  user_id      UUID NOT NULL REFERENCES identity.users(user_id),
  idempotency_key TEXT NOT NULL,
  operation    TEXT NOT NULL,
  resource_id  UUID,
  response_status INTEGER,
  response_body JSONB,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  expires_at   TIMESTAMPTZ NOT NULL,
  CONSTRAINT uq_idempotency_request UNIQUE (user_id, operation, idempotency_key)
);

CREATE INDEX idx_idempotency_requests_expiry
  ON payment.idempotency_requests (expires_at);
