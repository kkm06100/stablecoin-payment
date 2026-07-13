# API

## Payment service endpoints

```text
POST /v1/user-auth/signup
POST /v1/user-auth/login
POST /v1/user-auth/refresh
POST /v1/user-auth/logout

POST /v1/merchants/{merchantId}/payments
GET  /v1/merchants/{merchantId}/payments/{paymentId}
GET  /v1/merchants/{merchantId}/payments

GET  /v1/payment-qr/{rawToken}
POST /v1/payment-qr/{rawToken}/confirm
```

## stablecoin-transaction REST contract

Payment calls only these remote endpoints:

```text
POST /v1/wallets
GET  /v1/wallets/{walletId}
POST /v1/transfers
GET  /v1/transfers/{transferId}
GET  /v1/transfers?wallet_id={walletId}
```

Every remote request is signed with the existing
`x-nw-operator-id`, `x-nw-timestamp`, and `x-nw-signature` headers using the
canonical method/path/query/body rules. Payment JWTs are never forwarded to the
remote service.

## Transfer mapping

```json
{
  "src_wallet_id": "customer wallet id",
  "dst_wallet_id": "merchant settlement wallet id",
  "token": "USDC-test",
  "amount": "5500000",
  "reference_id": "payment_<payment_id>",
  "memo": "payment order <order_id>"
}
```

Remote 4xx responses are mapped to payment business errors. Timeout and 5xx are
`UNKNOWN_REMOTE_RESULT`; a recovery query must resolve the transfer before the
payment becomes `PAID` or `FAILED`.
