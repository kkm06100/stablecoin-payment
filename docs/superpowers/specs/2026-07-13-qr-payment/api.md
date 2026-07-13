# API

## 사용자 인증

```text
POST /v1/user-auth/signup
POST /v1/user-auth/login
POST /v1/user-auth/refresh
POST /v1/user-auth/logout
```

## 결제 생성

```http
POST /v1/merchants/{merchantId}/payments
Authorization: Bearer <access-token>
```

```json
{
  "order_id": "POS-01-20260713-000123",
  "token": "USDC-test",
  "amount": "5500000",
  "description": "coffee"
}
```

## QR

```text
GET  /v1/payment-qr/{rawToken}
POST /v1/payment-qr/{rawToken}/confirm   JWT required
```

## 오류 코드

```text
USER_NOT_FOUND
USER_SUSPENDED
MERCHANT_NOT_FOUND
MERCHANT_ACCESS_DENIED
MERCHANT_INACTIVE
PAYMENT_NOT_FOUND
PAYMENT_EXPIRED
PAYMENT_ALREADY_PROCESSED
QR_TOKEN_INVALID
```

기존 `DUPLICATE_REQUEST_MISMATCH`, `INSUFFICIENT_BALANCE`,
`UNSUPPORTED_TOKEN`과 wallet 오류를 재사용한다.
