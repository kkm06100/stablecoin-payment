# Database Conventions

## Migration boundaries

```text
V10__users.sql       identity schema and customer ownership
V11__merchants.sql   merchant schema, membership, settlement ownership
V12__payments.sql    payment schema, QR tokens, indexes
```

Each migration is forward-only and contains complete schema-qualified names.
Existing migrations are never edited. Foreign keys use PostgreSQL's default
`RESTRICT` behavior; payment/audit data is not cascade-deleted.

## Common column rules

- Application-generated `UUID` primary keys, matching existing entities.
- `TIMESTAMPTZ NOT NULL DEFAULT now()` for creation timestamps.
- Mutable aggregates have `updated_at`; services update it explicitly.
- Token amounts use `NUMERIC(40,0)` and Java `BigInteger`.
- Status and role columns use `TEXT`; Java constants own protocol values.
- Foreign keys are stored as UUID fields in JPA, not object graphs.
- Raw passwords, refresh tokens, and QR tokens are never stored.

## Validation split

The database owns structural integrity:

- primary/foreign/unique keys
- required columns
- positive payment amount
- at least one login identifier

The application owns changing policy:

- accepted status transitions
- role permissions
- email/phone/order-id syntax and length
- referenced wallet type/status
- QR TTL

## Naming and indexes

- Constraints: `uq_<table>_<purpose>`, `ck_<table>_<purpose>`.
- Indexes: `idx_<table>_<query-purpose>`.
- Every FK used for reverse lookup receives an index unless covered by the
  leading columns of a PK/unique index.
- Partial indexes are used for expiry workers and active wallet lookups.

## Data retention

User withdrawal and merchant closure are logical status changes. Rows referenced
by ledger, transfer, or payment history are retained. Refresh-token and expired
QR-token cleanup can physically delete old rows after a separate retention policy
is configured.
