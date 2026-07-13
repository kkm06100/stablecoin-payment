# Task 5: Payment schema and harness

Design: [V12 payment schema](../../../specs/2026-07-13-qr-payment/database/payment.md)

## Files

- `src/main/resources/db/migration/V12__payments.sql`
- `payment/Payment`, `PaymentRepository`, status/constants
- `payment/qr/PaymentQrToken`, repository
- `payment/PaymentPersistenceIT`

## Steps

- [ ] Create payments before payment_qr_tokens
- [ ] Add positive amount, merchant/order, transfer, and token-hash constraints
- [ ] Add merchant/customer history, CREATED expiry, and payment-token indexes
- [ ] Map entities using UUID FKs and String lifecycle constants
- [ ] Add conditional claim and batch expiry modifying queries
- [ ] Test duplicate creation race recovery contract
- [ ] Test two concurrent claims: exactly one update count must be 1
- [ ] Verify query plans use history/expiry indexes with representative fixtures

## Acceptance

All cases listed in the payment design's migration-verification section pass.
