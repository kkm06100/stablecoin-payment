# Task 2: Merchant schema and persistence

Design: [V11 merchant schema](../../../specs/2026-07-13-qr-payment/database/merchant.md)

## Files

- `src/main/resources/db/migration/V11__merchants.sql`
- `merchant/Merchant*`, `MerchantMember*`, `MerchantWallet*`
- `merchant/MerchantAuthorization`, role/status constants
- merchant persistence and authorization ITs

## Steps

- [ ] Create three tables in FK dependency order
- [ ] Add normalized business-number uniqueness
- [ ] Add reverse member lookup and active settlement partial indexes
- [ ] Map both composite keys using existing repository style
- [ ] Implement merchant+OWNER+USER-wallet+SETTLEMENT onboarding transaction
- [ ] Test multiple memberships, single wallet ownership, and single active settlement
- [ ] Test FK restriction and cross-merchant authorization denial

## Acceptance

All cases listed in the merchant design's migration-verification section pass.
