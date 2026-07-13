# V10 Identity Schema

## `identity.users`

| Column | Type | Constraint |
|---|---|---|
| user_id | UUID | PK |
| email | TEXT | nullable |
| phone | TEXT | nullable |
| password_hash | TEXT | NOT NULL |
| status | TEXT | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |

Constraints and indexes:

```sql
CONSTRAINT ck_users_login_identifier CHECK (email IS NOT NULL OR phone IS NOT NULL);
CREATE UNIQUE INDEX uq_users_email_normalized
  ON identity.users (lower(email)) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX uq_users_phone
  ON identity.users (phone) WHERE phone IS NOT NULL;
```

The service trims/lowercases email and stores phone in canonical E.164 format
before insert. `password_hash` contains the password encoder's self-describing
encoded value.

## `identity.refresh_tokens`

| Column | Type | Constraint |
|---|---|---|
| refresh_token_id | UUID | PK |
| user_id | UUID | FK users, NOT NULL |
| token_family_id | UUID | NOT NULL |
| token_hash | TEXT | UNIQUE, NOT NULL |
| expires_at | TIMESTAMPTZ | NOT NULL |
| consumed_at | TIMESTAMPTZ | nullable |
| revoked_at | TIMESTAMPTZ | nullable |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |

Indexes:

```sql
CREATE INDEX idx_refresh_tokens_user_active
  ON identity.refresh_tokens (user_id, expires_at)
  WHERE revoked_at IS NULL;
CREATE INDEX idx_refresh_tokens_family
  ON identity.refresh_tokens (token_family_id);
```

Refresh rotation marks the presented row `consumed_at` and inserts a new row in
the same family. Reuse of an already consumed token revokes every unrevoked row
in that family.

## `identity.customer_profiles`

| Column | Type | Constraint |
|---|---|---|
| customer_id | UUID | PK |
| user_id | UUID | UNIQUE FK users, NOT NULL |
| display_name | TEXT | NOT NULL |
| status | TEXT | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |

One user has at most one customer profile. Merchant membership is independent,
so the same user can also operate merchants.

## `identity.customer_wallets`

| Column | Type | Constraint |
|---|---|---|
| customer_id | UUID | FK customer_profiles, NOT NULL |
| wallet_id | UUID | UNIQUE FK wallet.wallets, NOT NULL |
| wallet_role | TEXT | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |

Primary key: `(customer_id, wallet_id)`.

```sql
CREATE UNIQUE INDEX uq_customer_wallets_primary
  ON identity.customer_wallets (customer_id)
  WHERE wallet_role = 'PRIMARY';
```

MVP permits one PRIMARY wallet. The service locks the wallet row and verifies
`wallet_type = USER` and `status = ACTIVE` before association.

## Migration verification

- duplicate normalized email fails
- duplicate phone fails
- user without email and phone fails
- second customer profile for one user fails
- one wallet cannot belong to two customers
- second PRIMARY wallet for one customer fails
- deleting a referenced user/wallet is restricted
