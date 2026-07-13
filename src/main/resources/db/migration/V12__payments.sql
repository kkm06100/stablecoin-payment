CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE payment.payments (
  payment_id          UUID PRIMARY KEY,
  merchant_id         UUID NOT NULL REFERENCES merchant.merchants(merchant_id),
  merchant_wallet_id  UUID NOT NULL REFERENCES wallet.wallets(wallet_id),
  created_by          UUID NOT NULL REFERENCES identity.users(user_id),
  order_id            TEXT NOT NULL,
  token               TEXT NOT NULL,
  amount              NUMERIC(40,0) NOT NULL,
  description         TEXT,
  status              TEXT NOT NULL,
  customer_id         UUID REFERENCES identity.customer_profiles(customer_id),
  customer_wallet_id  UUID REFERENCES wallet.wallets(wallet_id),
  transfer_id         UUID UNIQUE REFERENCES chain.internal_transfers(transfer_id),
  expires_at          TIMESTAMPTZ NOT NULL,
  processing_at       TIMESTAMPTZ,
  paid_at             TIMESTAMPTZ,
  cancelled_at        TIMESTAMPTZ,
  failure_code        TEXT,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_payments_merchant_order UNIQUE (merchant_id, order_id),
  CONSTRAINT ck_payments_positive_amount CHECK (amount > 0)
);

CREATE INDEX idx_payments_merchant_history
  ON payment.payments (merchant_id, created_at DESC, payment_id DESC);

CREATE INDEX idx_payments_customer_history
  ON payment.payments (customer_id, created_at DESC, payment_id DESC)
  WHERE customer_id IS NOT NULL;

CREATE INDEX idx_payments_created_expiry
  ON payment.payments (expires_at, payment_id)
  WHERE status = 'CREATED';

CREATE TABLE payment.payment_qr_tokens (
  qr_token_id  UUID PRIMARY KEY,
  payment_id   UUID NOT NULL REFERENCES payment.payments(payment_id),
  token_hash   TEXT NOT NULL UNIQUE,
  expires_at   TIMESTAMPTZ NOT NULL,
  used_at      TIMESTAMPTZ,
  revoked_at   TIMESTAMPTZ,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_qr_tokens_payment
  ON payment.payment_qr_tokens (payment_id, created_at DESC);
