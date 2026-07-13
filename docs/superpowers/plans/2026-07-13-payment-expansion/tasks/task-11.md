# Task 11: Payment 상태·이력·멱등성 기반

- [ ] `CREATED`, `PROCESSING`, `PAID`, `FAILED`, `EXPIRED`, `CANCELLED`, `REFUND_PENDING`, `REFUNDED` 전이 정의
- [ ] payment/transfer 상태 이력 migration 및 repository 추가
- [ ] idempotency key unique 제약과 중복 요청 응답 추가
- [ ] 동시 confirm 및 재시도 테스트
