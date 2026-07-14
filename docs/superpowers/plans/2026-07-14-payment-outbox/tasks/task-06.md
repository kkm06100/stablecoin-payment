# Task 06: Mock 기반 IT와 커버리지

## 외부 서비스 처리

실제 외부 서비스는 띄우지 않는다.

- Solana chain: mock
- OpenBao: mock
- Stablecoin Transaction server: mock

IT에서는 실제 PostgreSQL, Spring Context, Security, Controller, Service, Repository, Outbox Worker를 사용한다.

외부 경계는 `StablecoinTransactionClient` mock으로 대체한다.

## 필수 IT

### Outbox 생성

- 결제 승인 요청
- 결제 상태가 PROCESSING인지 확인
- Outbox가 PENDING으로 저장되는지 확인
- idempotency key가 `payment_<payment_id>`인지 확인

### Worker 성공

- mock 송금 성공
- payment가 PAID인지 확인
- transfer id가 저장되는지 확인
- QR token이 USED인지 확인
- Outbox가 SUCCEEDED인지 확인

### Worker 실패/재시도

- mock 5xx 또는 timeout 반환
- retry count 증가 확인
- next retry time 저장 확인
- 최대 재시도 후 FAILED 확인

### 중복 방지

- 같은 Outbox 이벤트를 두 Worker가 처리하는 상황
- 같은 idempotency key로 외부 API가 중복 호출되는 상황
- 기존 transfer 결과를 재사용하는지 확인

### 재시도 제외

- `InternalApplicationException` 발생
- 재시도하지 않고 FAILED 처리되는지 확인

## 커버리지 기준

- Outbox Worker 성공 분기
- 외부 API timeout 분기
- retryable/non-retryable 분기
- 최대 재시도 분기
- 중복 이벤트 분기

JaCoCo branch report를 기준으로 위 분기가 실제로 실행됐는지 확인한다.
