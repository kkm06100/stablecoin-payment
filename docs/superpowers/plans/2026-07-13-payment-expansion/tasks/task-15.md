# Task 15: 송금 복구·결제 취소·환불

- [ ] timeout 후 transfer 조회를 통한 상태 복구
- [ ] `POST /v1/merchants/{merchantId}/payments/{paymentId}/cancel`
- [ ] 미승인 결제 취소 및 승인 결제 refund transfer 처리
- [ ] `REFUND_PENDING`, `REFUNDED` 전이와 관리자 권한 검증
- [ ] 중복 취소·환불 실패 테스트
