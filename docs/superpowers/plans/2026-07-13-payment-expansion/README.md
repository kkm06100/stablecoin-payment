---
plan_id: payment-expansion
---

# Payment Expansion Implementation Plan

설계: [QR Payment Design](../../specs/2026-07-13-qr-payment/README.md)

## 원칙

- Payment는 `stablecoin-transaction`을 REST로만 호출한다.
- 결제/송금의 외부 ID와 내부 상태를 매핑하고 모든 재시도는 멱등 처리한다.
- 기존 QR 결제 API와 DTO snake_case, 예외 코드, 통합 테스트 규칙을 유지한다.

## 작업

| # | 작업 | 문서 |
|---|---|---|
| 11 | Payment 상태·이력·멱등성 기반 | [task-11](tasks/task-11.md) |
| 12 | 고객 결제내역·상세 조회 | [task-12](tasks/task-12.md) |
| 13 | 고객 지갑·잔액 조회 | [task-13](tasks/task-13.md) |
| 14 | 일반 송금 REST API | [task-14](tasks/task-14.md) |
| 15 | 송금 복구·결제 취소·환불 | [task-15](tasks/task-15.md) |
| 16 | 가맹점 매출·정산 조회 | [task-16](tasks/task-16.md) |
| 17 | 프론트 시나리오·통합 검증 | [task-17](tasks/task-17.md) |

각 task는 해당 Java 패키지, migration, 테스트 범위만 변경한다.
