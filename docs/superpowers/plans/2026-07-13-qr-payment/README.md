---
plan_id: qr-payment
---

# QR Payment Implementation Plan

설계: [QR Payment Design](../../specs/2026-07-13-qr-payment/README.md)

## 컨벤션

- 기능 중심 최상위 패키지, 구체 Service/Component, UUID FK 직접 매핑
- DTO는 snake_case `record`, 상수는 소유 패키지, 오류는 `ApiErrorCodes`
- `PostgresIntegrationTest`와 실제 원장 사용, given/when/then 테스트
- 기존 `/v1/*`와 운영자 Ed25519 인증 회귀 금지

## 작업

| # | 작업 | 문서 |
|---|---|---|
| 1 | Identity schema/persistence | [task-01](tasks/task-01.md) |
| 2 | Merchant schema/persistence | [task-02](tasks/task-02.md) |
| 3 | JWT authentication boundary | [task-03](tasks/task-03.md) |
| 4 | Merchant lifecycle API | [task-04](tasks/task-04.md) |
| 5 | Payment schema/harness | [task-05](tasks/task-05.md) |
| 6 | Merchant payment creation | [task-06](tasks/task-06.md) |
| 7 | Public QR lookup | [task-07](tasks/task-07.md) |
| 8 | Customer confirmation | [task-08](tasks/task-08.md) |
| 9 | Concurrency/idempotency | [task-09](tasks/task-09.md) |
| 10 | Query, expiry, docs | [task-10](tasks/task-10.md) |

각 task가 시작될 때만 해당 Java 패키지와 파일을 생성한다.
