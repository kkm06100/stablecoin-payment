# Task 8: Customer payment confirmation

설계: [flows](../../../specs/2026-07-13-qr-payment/flows.md)

## Steps

- [ ] JWT 보호 `POST /v1/payment-qr/{token}/confirm`
- [ ] JWT subject로 customer와 source wallet 결정
- [ ] 조건부 update로 payment claim
- [ ] 같은 DB 트랜잭션에서 `InternalTransferService` 직접 호출
- [ ] reference_id를 `payment_<payment_id>`로 고정
- [ ] PAID/transfer_id 저장과 오류 매핑
- [ ] claim, internal transfer, PAID update가 동일 outer transaction인지 검증
- [ ] 동기 business rejection은 전체 rollback되어 CREATED가 유지되는지 검증
- [ ] 성공, 잔액 부족, 만료, 비활성 사용자/지갑 IT

## Acceptance

성공 시 payment, internal transfer, ledger posting이 일관되게 commit된다.
