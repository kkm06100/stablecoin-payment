# V11 Merchant Schema

## `merchant.merchants`

| Column | Type | Constraint |
|---|---|---|
| merchant_id | UUID | PK |
| merchant_name | TEXT | NOT NULL |
| business_number | TEXT | nullable |
| status | TEXT | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |

```sql
CREATE UNIQUE INDEX uq_merchants_business_number
  ON merchant.merchants (business_number)
  WHERE business_number IS NOT NULL;
```

Business number normalization is performed before insert. Merchant closure is a
status transition; the row is not deleted.

## `merchant.merchant_members`

| Column | Type | Constraint |
|---|---|---|
| merchant_id | UUID | FK merchants, NOT NULL |
| user_id | UUID | FK identity.users, NOT NULL |
| member_role | TEXT | NOT NULL |
| status | TEXT | NOT NULL |
| joined_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |

Primary key: `(merchant_id, user_id)`.

```sql
CREATE INDEX idx_merchant_members_user
  ON merchant.merchant_members (user_id, status, merchant_id);
```

Roles are `OWNER`, `MANAGER`, `CASHIER`, `VIEWER`. Statuses are `ACTIVE`,
`SUSPENDED`, `REMOVED`. These are application-owned constants so permission
policy can evolve without rewriting a database CHECK constraint.

## `merchant.merchant_wallets`

| Column | Type | Constraint |
|---|---|---|
| merchant_id | UUID | FK merchants, NOT NULL |
| wallet_id | UUID | UNIQUE FK wallet.wallets, NOT NULL |
| wallet_role | TEXT | NOT NULL |
| status | TEXT | NOT NULL |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |

Primary key: `(merchant_id, wallet_id)`.

```sql
CREATE UNIQUE INDEX uq_merchant_wallets_active_settlement
  ON merchant.merchant_wallets (merchant_id)
  WHERE wallet_role = 'SETTLEMENT' AND status = 'ACTIVE';
```

One wallet cannot be assigned to multiple merchants because `wallet_id` is
unique. The service verifies active USER wallet type while associating it.

## Write transaction

Merchant onboarding writes the following in one transaction:

1. insert merchant
2. insert requesting user as active OWNER
3. provision/reuse an existing USER wallet through `WalletService`
4. insert active SETTLEMENT mapping

Any failure rolls back all four writes.

## Migration verification

- duplicate business number fails
- duplicate member pair fails
- a user can join multiple merchants
- a wallet cannot belong to two merchants
- a merchant cannot have two active SETTLEMENT wallets
- deleting referenced users, merchants, or wallets is restricted
