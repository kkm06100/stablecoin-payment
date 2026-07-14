# Task 04: Retry와 Idempotency

## idempotency 결정

결제별 key는 다음 형식으로 고정한다.

```text
payment_<payment_id>
```

동일 key 재요청은 외부 Transaction 서버에서 새 송금을 만들지 않고 기존 결과를 반환해야 한다.

## timeout 처리

외부 API timeout은 성공/실패를 알 수 없는 상태다.

```text
timeout
  → 같은 idempotency key로 기존 transfer 조회
  → 존재하면 해당 transfer를 성공 결과로 반영
  → 없으면 retry
```

## `@Retryable` 위치

`@Retryable`은 `PaymentConfirmationService`가 아니라 Outbox Worker 또는 외부 API adapter에만 적용한다.

```java
@Retryable(
    retryFor = StablecoinTransactionRemoteException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
```

## 재시도 대상

- connection timeout
- network error
- HTTP 5xx
- HTTP 429

## 재시도 제외

- HTTP 400
- HTTP 401/403
- 잔액 부족
- 잘못된 지갑 주소
- 지원하지 않는 토큰
- `InternalApplicationException`

`InternalApplicationException`은 설정, 암호화, 직렬화 등 내부 오류이므로 재시도하지 않는다.

## 복구

- `@Recover` 또는 Worker의 실패 저장 로직으로 최종 실패를 기록한다.
- 재시도 횟수와 마지막 오류를 Outbox에 저장한다.
- 수동 재처리를 위해 FAILED 이벤트를 조회할 수 있어야 한다.
