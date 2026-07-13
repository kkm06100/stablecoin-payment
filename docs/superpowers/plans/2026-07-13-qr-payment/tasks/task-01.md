# Task 1: Identity schema and persistence

Design: [V10 identity schema](../../../specs/2026-07-13-qr-payment/database/identity.md)

## Files

- `src/main/resources/db/migration/V10__users.sql`
- `user/User`, `UserRepository`, `UserStatus`
- `user/CustomerProfile`, `CustomerProfileRepository`
- `user/CustomerWallet`, `CustomerWalletId`, `CustomerWalletRepository`
- `user/UserPersistenceIT`

## Steps

- [ ] Create `identity` schema and four tables in dependency order
- [ ] Add normalized email/phone unique indexes and login-identifier CHECK
- [ ] Add refresh token family, consumed/revoked timestamps, active/family indexes
- [ ] Add one-profile and one-primary-wallet constraints
- [ ] Map entities with UUID fields and composite-id style already used in repo
- [ ] Verify every constraint and default timestamp with PostgreSQL IT
- [ ] Verify FK deletion is restricted and existing Flyway migrations still pass

## Acceptance

All cases listed in the identity design's migration-verification section pass.
