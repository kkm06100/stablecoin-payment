# Task 05: 상태 조회와 Merchant 확장

## 결제 상태 조회

Outbox 비동기 처리 이후 결제 상태는 다음과 같이 조회한다.

```text
PROCESSING → PAID
PROCESSING → FAILED
```

- 기존 결제 상세 API가 최신 status를 반환하도록 한다.
- 프론트는 승인 응답이 PROCESSING이면 상세 API를 polling한다.
- PAID 확인 전에는 성공 UI를 표시하지 않는다.
- FAILED면 재시도 대기 또는 사용자 오류를 표시한다.

## Merchant 프로비저닝

결제 Outbox가 안정화된 이후 Merchant 생성에도 Outbox를 적용한다.

```text
Merchant PROVISIONING
  → wallet 생성 이벤트
  → 외부 wallet 생성
  → token account 등록
  → Merchant ACTIVE
```

실패 상태:

```text
PROVISIONING_FAILED
```

Merchant 생성 요청에서는 외부 지갑 생성 완료 전 `ACTIVE`를 반환하지 않는다.

## Merchant 이벤트

- `MERCHANT_WALLET_CREATE`
- `MERCHANT_TOKEN_ACCOUNT_REGISTER`

Merchant 이벤트도 idempotency key를 사용한다.

## 범위 제한

Merchant Outbox는 결제 Outbox의 성공 후 2차 작업으로 진행한다. 결제 승인 Outbox 구현이 완료되기 전에는 Merchant workflow를 동시에 변경하지 않는다.
