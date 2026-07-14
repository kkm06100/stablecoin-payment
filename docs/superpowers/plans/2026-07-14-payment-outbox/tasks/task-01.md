# Task 01: Payment Outbox 데이터 모델

## 목표

결제 DB 상태 변경과 외부 송금 요청을 같은 DB 트랜잭션에서 기록할 수 있도록 Outbox 저장 구조를 만든다.

## 구현 내용

- `V14__payment_outbox.sql` 추가
- `payment.payment_outbox` 테이블 생성
- `PaymentOutbox` Entity 추가
- `PaymentOutboxRepository` 추가
- `PaymentOutboxStatuses`와 event type 상수 추가

## 테이블 결정

```text
event_id              UUID PRIMARY KEY
payment_id            UUID NOT NULL REFERENCES payment.payments(payment_id)
event_type            TEXT NOT NULL
idempotency_key       TEXT NOT NULL UNIQUE
payload               JSONB NOT NULL
status                TEXT NOT NULL
attempt_count         INTEGER NOT NULL DEFAULT 0
next_attempt_at       TIMESTAMPTZ NOT NULL
locked_at             TIMESTAMPTZ
external_transfer_id  UUID
last_error            TEXT
created_at            TIMESTAMPTZ NOT NULL
updated_at            TIMESTAMPTZ NOT NULL
```

## 상태

```text
PENDING → PROCESSING → SUCCEEDED
                    ↘ FAILED
FAILED → PROCESSING
```

- `PENDING`: 아직 외부 API를 호출하지 않음
- `PROCESSING`: Worker가 처리 중
- `SUCCEEDED`: 외부 송금과 결제 완료 반영까지 끝남
- `FAILED`: 재시도 대기 또는 최대 재시도 초과

## 조회/동시성

- `status = PENDING` 또는 재시도 시간이 지난 `FAILED`만 조회
- `next_attempt_at`, `created_at` 복합 인덱스 추가
- Worker가 row를 선점할 때 `FOR UPDATE SKIP LOCKED` 사용
- 같은 `idempotency_key`는 하나만 생성되도록 UNIQUE 제약 적용

## 검증

- migration 적용
- Outbox insert/read 테스트
- 동일 idempotency key 중복 저장 거부 테스트
- payment 삭제 시 Outbox FK 정책 확인
