# V12 Payment Schema and Locking

## `payment.payments`

| Column | Type | Constraint |
|---|---|---|
| payment_id | UUID | PK |
| merchant_id | UUID | FK merchants, NOT NULL |
| merchant_wallet_id | UUID | FK wallet.wallets, NOT NULL |
| created_by | UUID | FK identity.users, NOT NULL |
| order_id | TEXT | NOT NULL |
| token | TEXT | NOT NULL |
| amount | NUMERIC(40,0) | NOT NULL, amount > 0 |
| description | TEXT | nullable |
| status | TEXT | NOT NULL |
| customer_id | UUID | nullable FK customer_profiles |
| customer_wallet_id | UUID | nullable FK wallet.wallets |
| transfer_id | UUID | nullable UNIQUE FK internal_transfers |
| expires_at | TIMESTAMPTZ | NOT NULL |
| processing_at | TIMESTAMPTZ | nullable |
| paid_at | TIMESTAMPTZ | nullable |
| cancelled_at | TIMESTAMPTZ | nullable |
| failure_code | TEXT | nullable |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |
| updated_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |

```sql
CONSTRAINT uq_payments_merchant_order UNIQUE (merchant_id, order_id);
CONSTRAINT ck_payments_positive_amount CHECK (amount > 0);

CREATE INDEX idx_payments_merchant_history
  ON payment.payments (merchant_id, created_at DESC, payment_id DESC);
CREATE INDEX idx_payments_customer_history
  ON payment.payments (customer_id, created_at DESC, payment_id DESC)
  WHERE customer_id IS NOT NULL;
CREATE INDEX idx_payments_created_expiry
  ON payment.payments (expires_at, payment_id)
  WHERE status = 'CREATED';
```

Merchant/customer wallet ids are snapshots. A later default-wallet change does
not change historical payment ownership.

## `payment.payment_qr_tokens`

| Column | Type | Constraint |
|---|---|---|
| qr_token_id | UUID | PK |
| payment_id | UUID | FK payments, NOT NULL |
| token_hash | TEXT | UNIQUE, NOT NULL |
| expires_at | TIMESTAMPTZ | NOT NULL |
| used_at | TIMESTAMPTZ | nullable |
| revoked_at | TIMESTAMPTZ | nullable |
| created_at | TIMESTAMPTZ | NOT NULL DEFAULT now() |

```sql
CREATE INDEX idx_payment_qr_tokens_payment
  ON payment.payment_qr_tokens (payment_id, created_at DESC);
```

Multiple token rows allow QR reissue. Reissue revokes the previous active token
in the same transaction. A time-dependent partial unique index is deliberately
avoided because PostgreSQL index predicates cannot safely depend on `now()`.

## Creation idempotency

Creation first queries `(merchant_id, order_id)`. A concurrent insert race is
resolved by `uq_payments_merchant_order`: after unique violation, reload and
compare merchant wallet, token, amount, and description policy. Identical input
returns the existing payment; mismatch returns 409.

Payment and its first QR token are inserted in one transaction. QR TTL is derived
from one injected Clock value so payment/token `expires_at` are identical.

## Confirmation claim

Repository uses a modifying query equivalent to:

```sql
UPDATE payment.payments
SET status = 'PROCESSING',
    customer_id = :customer_id,
    customer_wallet_id = :customer_wallet_id,
    processing_at = :now,
    updated_at = :now
WHERE payment_id = :payment_id
  AND status = 'CREATED'
  AND expires_at > :now;
```

Update count `1` owns the claim. Update count `0` triggers a reload to distinguish
expired, cancelled, paid, and processing outcomes.

The same outer transaction then calls `InternalTransferService` with reference
`payment_<payment_id>` and updates:

```sql
UPDATE payment.payments
SET status = 'PAID', transfer_id = :transfer_id,
    paid_at = :now, updated_at = :now
WHERE payment_id = :payment_id AND status = 'PROCESSING';
```

Because the transfer service joins the outer transaction, insufficient balance
or another exception rolls back the claim and all ledger writes. No persistent
`FAILED` row is written for a synchronous business rejection in MVP.

## Expiry worker

Batch update uses the partial expiry index:

```sql
UPDATE payment.payments
SET status = 'EXPIRED', updated_at = :now
WHERE status = 'CREATED' AND expires_at <= :now;
```

Every lookup and confirmation still checks `expires_at`; scheduler delay never
extends QR validity.

## Migration verification

- non-positive amount fails
- duplicate merchant/order fails
- one transfer cannot belong to two payments
- QR token hash is globally unique
- one concurrent claim returns 1 and all others return 0
- history and expiry queries use their intended indexes with representative data
- referenced payment history cannot be cascade-deleted
