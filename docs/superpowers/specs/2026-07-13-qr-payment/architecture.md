# Architecture

## Package boundaries

```text
stablecointransaction.user       users and customer ownership
stablecointransaction.userauth   JWT authentication and refresh sessions
stablecointransaction.merchant   merchant membership and settlement ownership
stablecointransaction.payment    payment, QR, and confirmation orchestration
stablecointransaction.client     stablecoin-transaction REST client and signer
```

The existing `auth` package remains the operator Ed25519 implementation. Payment
code must not import `InternalTransferService`, `WalletService`, repositories, or
ledger classes from stablecoin-transaction.

## Service boundary

```text
payment service                         stablecoin-transaction
PaymentConfirmer
  -> StablecoinTransactionClient
       POST /v1/transfers  --x-nw-*-->  InternalTransferService -> ledger DB
```

Payment does not access wallet, ledger, chain, or OpenBao data directly. Wallet
provisioning and value movement happen through stablecoin-transaction REST.

The REST adapter owns `RestClient`, canonical message construction, OpenBao
Ed25519 signing, timeout policy, and response mapping. Controllers and payment
services depend only on the client port.

## Transaction boundary

Payment DB and stablecoin-transaction DB are separate transactions. Payment
confirmation uses `CREATED -> PROCESSING` and deterministic
`reference_id = payment_<payment_id>`. A timeout or 5xx never immediately means
failure: recovery queries the transfer endpoint and then moves the payment to
`PAID` or `FAILED`.
