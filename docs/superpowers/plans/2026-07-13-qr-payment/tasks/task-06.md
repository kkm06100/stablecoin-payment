# Task 6: Merchant payment creation

설계: [flows](../../../specs/2026-07-13-qr-payment/flows.md),
[api](../../../specs/2026-07-13-qr-payment/api.md)

## Steps

- [ ] snake_case request/response record와 Bean Validation
- [ ] PaymentValidator, PaymentRequestMatcher 구현
- [ ] 멤버 권한과 정산 지갑을 확인하는 PaymentCreator 구현
- [ ] 128-bit 이상 난수 생성, SHA-256 해시만 저장
- [ ] `POST /v1/merchants/{merchantId}/payments` 및 IT

## Acceptance

동일 주문 재시도는 같은 결제를 반환하고 불변 필드 불일치는 409다.
