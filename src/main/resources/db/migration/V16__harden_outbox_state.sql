ALTER TABLE payment.payment_outbox
  ADD COLUMN qr_token_id UUID REFERENCES payment.payment_qr_tokens(qr_token_id);

UPDATE payment.payment_outbox o
SET qr_token_id = (
  SELECT t.qr_token_id
  FROM payment.payment_qr_tokens t
  WHERE t.payment_id = o.payment_id
  ORDER BY t.created_at DESC
  LIMIT 1
)
WHERE o.qr_token_id IS NULL;

ALTER TABLE payment.payment_outbox
  ALTER COLUMN qr_token_id SET NOT NULL;

ALTER TABLE payment.payment_outbox
  ADD CONSTRAINT payment_outbox_status_check
  CHECK (status IN ('PENDING', 'PROCESSING', 'FAILED', 'SUCCEEDED', 'DEAD'));

ALTER TABLE merchant.merchant_outbox
  ADD CONSTRAINT merchant_outbox_status_check
  CHECK (status IN ('PENDING', 'PROCESSING', 'FAILED', 'SUCCEEDED', 'DEAD'));

CREATE INDEX idx_payment_outbox_processing_lock
  ON payment.payment_outbox (status, locked_at);

CREATE INDEX idx_merchant_outbox_processing_lock
  ON merchant.merchant_outbox (status, locked_at);
