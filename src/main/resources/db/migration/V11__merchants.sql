CREATE SCHEMA IF NOT EXISTS merchant;

CREATE TABLE merchant.merchants (
  merchant_id      UUID PRIMARY KEY,
  merchant_name    TEXT NOT NULL,
  business_number  TEXT,
  status           TEXT NOT NULL,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_merchants_business_number
  ON merchant.merchants (business_number)
  WHERE business_number IS NOT NULL;

CREATE TABLE merchant.merchant_members (
  merchant_id  UUID NOT NULL REFERENCES merchant.merchants(merchant_id),
  user_id      UUID NOT NULL REFERENCES identity.users(user_id),
  member_role  TEXT NOT NULL,
  status       TEXT NOT NULL,
  joined_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (merchant_id, user_id)
);

CREATE INDEX idx_merchant_members_user
  ON merchant.merchant_members (user_id, status, merchant_id);

CREATE TABLE merchant.merchant_wallets (
  merchant_id  UUID NOT NULL REFERENCES merchant.merchants(merchant_id),
  wallet_id    UUID NOT NULL UNIQUE REFERENCES wallet.wallets(wallet_id),
  wallet_role  TEXT NOT NULL,
  status       TEXT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (merchant_id, wallet_id)
);

CREATE UNIQUE INDEX uq_merchant_wallets_active_settlement
  ON merchant.merchant_wallets (merchant_id)
  WHERE wallet_role = 'SETTLEMENT' AND status = 'ACTIVE';
