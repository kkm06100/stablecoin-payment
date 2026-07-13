# Scope

## 포함

- JWT access token과 회전형 refresh token
- 사용자, 고객 프로필, 고객 USER 지갑 연결
- 가맹점, 소속 사용자, 정산 USER 지갑 연결
- `(merchant_id, order_id)` 기반 결제 생성 멱등성
- PostgreSQL 기준 5분 QR 토큰
- 고객 결제 승인과 단일 내부 이체
- 가맹점 결제 조회

## 제외

- Redis QR 캐시
- 환불 및 정산 배치
- SSE/WebSocket 알림
- KYC/AML 및 운영 키 회전
