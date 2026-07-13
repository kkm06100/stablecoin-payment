# Data Model

Database design is split by migration boundary so an implementation task only
loads the schema it owns.

- [Database conventions](./database/conventions.md)
- [V10 identity schema](./database/identity.md)
- [V11 merchant schema](./database/merchant.md)
- [V12 payment schema and locking](./database/payment.md)

## Relationship summary

```text
identity.users 1---0..1 identity.customer_profiles
identity.users *---* merchant.merchants (merchant_members)

customer_profiles 1---1 wallet.wallets (customer_wallets)
merchants         1---1 wallet.wallets (merchant_wallets)

merchants 1---* payment.payments
payments  1---* payment.payment_qr_tokens
payments  1---0..1 chain.internal_transfers
```
