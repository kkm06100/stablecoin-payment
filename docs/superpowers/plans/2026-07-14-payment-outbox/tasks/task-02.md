# Task 02: Payment Confirmation Outbox 전환

## 현재 문제

현재 `PaymentConfirmationService`가 다음 작업을 하나의 DB 트랜잭션에서 수행한다.

```text
claim → 외부 송금 → PAID → QR USED
```

외부 송금이 성공한 뒤 `markPaid` 또는 QR 상태 변경이 실패하면 DB만 rollback되고 외부 송금은 되돌릴 수 없다.

## 변경 후 흐름

```text
Transaction A
  - QR와 결제 검증
  - 결제 CREATED → PROCESSING
  - payment_outbox INSERT(PENDING)
  - COMMIT

Response
  - status = PROCESSING

Transaction B(Worker)
  - Outbox 선점
  - 외부 송금
  - 송금 결과 저장
  - 결제 PROCESSING → PAID
  - QR USED
  - Outbox SUCCEEDED
  - COMMIT
```

## API 결정

- 초기에는 기존 응답 형식을 유지한다.
- HTTP status는 `200`으로 유지한다.
- 단, 외부 송금 완료 전에는 `status = PROCESSING`을 반환한다.
- `PAID`를 미리 반환하지 않는다.
- 추후 API 버전에서 `202 Accepted`와 operation resource로 변경할 수 있다.

## 구현 내용

- `PaymentConfirmationService`에서 `StablecoinTransactionClient` 직접 호출 제거
- 외부 송금 대신 Outbox 이벤트 저장
- `PaymentClaimProcessor`는 결제 claim만 담당
- `PaymentOutboxWriter`는 Outbox 생성만 담당
- 기존 QR replay 처리 유지
- `PaymentCompletionProcessor`는 Worker 전용 완료 처리로 변경

## 트랜잭션 경계

- 외부 API 호출은 `@Transactional` 메서드 안에서 수행하지 않는다.
- 결제 PROCESSING과 Outbox INSERT는 같은 트랜잭션으로 묶는다.
- Worker의 Outbox 상태 변경과 결제 완료 반영은 별도 DB 트랜잭션으로 묶는다.

## 실패 처리

- 외부 API 실패 시 Outbox를 FAILED로 변경한다.
- 결제는 PROCESSING 상태를 유지하고 Worker가 재시도한다.
- 최대 재시도 초과 시 FAILED 이벤트와 failure reason을 저장한다.
- 외부 송금 성공 여부가 불명확한 timeout은 바로 새 송금을 만들지 않는다.
