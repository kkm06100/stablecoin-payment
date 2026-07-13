# Task 7: Public QR lookup

설계: [flows](../../../specs/2026-07-13-qr-payment/flows.md)

## Steps

- [ ] `GET /v1/payment-qr/{token}` 구현
- [ ] 입력 토큰을 해시하여 PostgreSQL 조회
- [ ] token/payment 상태와 expires_at 검증
- [ ] 지갑 ID와 사용자 ID를 제외한 표시 필드만 응답
- [ ] valid/invalid/revoked/expired/cancelled IT 작성

## Acceptance

Redis 없이 DB가 QR 유효성의 단일 진실 공급원으로 동작한다.
