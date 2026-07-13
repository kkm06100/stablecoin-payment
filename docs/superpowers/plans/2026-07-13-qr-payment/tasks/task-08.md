# Task 8: Customer payment confirmation

Design: [flows](../../../specs/2026-07-13-qr-payment/flows.md)

## Steps

- [ ] Protect `POST /v1/payment-qr/{token}/confirm` with customer JWT
- [ ] Resolve customer and source wallet through payment-side ownership data
- [ ] Claim payment with one conditional DB update
- [ ] Call `StablecoinTransactionClient` with signed `POST /v1/transfers`
- [ ] Fix reference to `payment_<payment_id>`
- [ ] Persist remote `transfer_id` and mark payment `PAID`
- [ ] Treat remote 4xx as business failure and timeout/5xx as unknown
- [ ] Add recovery worker using transfer GET/list endpoints
- [ ] Test retry, timeout recovery, insufficient balance, and duplicate transfer

## Acceptance

Payment code has no direct dependency on `InternalTransferService`, wallet
repositories, ledger repositories, or chain clients.
