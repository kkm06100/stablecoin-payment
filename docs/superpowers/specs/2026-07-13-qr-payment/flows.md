# Flows

## Payment creation

```text
JWT merchant user
 -> membership and settlement wallet authorization
 -> payment DB CREATED + QR token
```

The settlement wallet is resolved through the stablecoin-transaction REST client
or a previously stored remote wallet id. Payment never opens the wallet database.

## Customer confirmation

```text
JWT customer
 -> payment DB conditional claim: CREATED -> PROCESSING
 -> StablecoinTransactionClient
      signed POST /v1/transfers
      reference_id = payment_<payment_id>
 -> transfer_id persisted
 -> payment PAID
```

The remote transfer API's reference-id idempotency is the second duplicate-debit
guard. If the POST times out, a recovery worker calls
`GET /v1/transfers/{transferId}` or the wallet transfer list before deciding the
payment result. The two databases cannot be rolled back as one transaction.

## State

```text
CREATED -> PROCESSING -> PAID
                    \-> FAILED
CREATED -> EXPIRED
CREATED -> CANCELLED
```
