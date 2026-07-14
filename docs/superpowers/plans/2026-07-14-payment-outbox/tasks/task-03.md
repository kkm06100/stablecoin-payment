# Task 03: Outbox Worker

## 책임

`PaymentOutboxScheduler`는 주기적으로 Worker를 호출하고, 실제 처리는 `PaymentOutboxProcessor`가 담당한다.

```text
PaymentOutboxScheduler
  → PaymentOutboxProcessor
  → PaymentTransferProcessor
  → PaymentCompletionProcessor
```

## 처리 순서

1. 처리 가능한 Outbox row를 lock으로 선점한다.
2. `PENDING` 또는 재시도 시간이 지난 `FAILED`를 `PROCESSING`으로 변경한다.
3. commit 후 외부 송금을 호출한다.
4. 성공하면 transfer id를 저장한다.
5. 결제를 `PAID`로 변경한다.
6. QR 토큰을 `USED`로 변경한다.
7. Outbox를 `SUCCEEDED`로 변경한다.

## 실패 순서

1. 외부 API 예외를 분류한다.
2. 재시도 가능하면 attempt count를 증가시킨다.
3. `next_attempt_at`을 backoff 기준으로 계산한다.
4. `last_error`를 저장한다.
5. 재시도 불가능하거나 횟수를 초과하면 `FAILED`로 확정한다.

## 상태 복구

- Worker가 죽어 `PROCESSING`에 멈춘 row는 `locked_at` timeout으로 재처리한다.
- `SUCCEEDED` 이벤트는 다시 외부 API를 호출하지 않는다.
- 외부 송금 결과가 이미 존재하면 idempotency key로 결과를 조회한다.

## 구현 파일

- `PaymentOutboxScheduler`
- `PaymentOutboxProcessor`
- `PaymentOutboxRepository`
- `PaymentOutboxClaimRepository` 또는 lock query
- `PaymentOutboxRecoveryProcessor`

## 주의

Worker만 외부 API를 호출한다. Controller, `PaymentConfirmationService`, 일반 DB transaction component에서는 외부 API를 호출하지 않는다.
