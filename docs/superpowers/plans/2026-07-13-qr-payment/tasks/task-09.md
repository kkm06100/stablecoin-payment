# Task 9: Concurrency and idempotency

## Steps

- [ ] 두 동시 confirm을 실행하는 `PaymentConcurrencyIT`
- [ ] payment 전이 1회, transfer 1개, debit/credit 1회 검증
- [ ] postings 합계 0 검증
- [ ] 응답 유실을 가정한 confirm 재시도 검증
- [ ] 동일 order 동일/불일치 요청 검증

## Acceptance

동시성과 재시도 상황에서도 고객 잔액은 정확히 한 번만 차감된다.
