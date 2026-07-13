CREATE SCHEMA IF NOT EXISTS identity;

CREATE TABLE identity.users (
  user_id        UUID PRIMARY KEY,
  email          TEXT,
  phone          TEXT,
  password_hash  TEXT NOT NULL,
  status         TEXT NOT NULL,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT ck_users_login_identifier
    CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

CREATE UNIQUE INDEX uq_users_email_normalized
  ON identity.users (lower(email))
  WHERE email IS NOT NULL;

CREATE UNIQUE INDEX uq_users_phone
  ON identity.users (phone)
  WHERE phone IS NOT NULL;

CREATE TABLE identity.refresh_tokens (
  refresh_token_id  UUID PRIMARY KEY,
  user_id           UUID NOT NULL REFERENCES identity.users(user_id),
  token_family_id   UUID NOT NULL,
  token_hash        TEXT NOT NULL UNIQUE,
  expires_at        TIMESTAMPTZ NOT NULL,
  consumed_at       TIMESTAMPTZ,
  revoked_at        TIMESTAMPTZ,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_active
  ON identity.refresh_tokens (user_id, expires_at)
  WHERE revoked_at IS NULL;

CREATE INDEX idx_refresh_tokens_family
  ON identity.refresh_tokens (token_family_id);

CREATE TABLE identity.customer_profiles (
  customer_id  UUID PRIMARY KEY,
  user_id      UUID NOT NULL UNIQUE REFERENCES identity.users(user_id),
  display_name TEXT NOT NULL,
  status       TEXT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE identity.customer_wallets (
  customer_id  UUID NOT NULL REFERENCES identity.customer_profiles(customer_id),
  wallet_id    UUID NOT NULL UNIQUE,
  wallet_role  TEXT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (customer_id, wallet_id)
);

CREATE UNIQUE INDEX uq_customer_wallets_primary
  ON identity.customer_wallets (customer_id)
  WHERE wallet_role = 'PRIMARY';
