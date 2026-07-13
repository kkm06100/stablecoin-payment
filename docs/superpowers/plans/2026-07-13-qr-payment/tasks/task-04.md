# Task 4: Merchant lifecycle API

설계: [auth](../../../specs/2026-07-13-qr-payment/auth.md),
[api](../../../specs/2026-07-13-qr-payment/api.md)

## Steps

- [ ] 가맹점 생성과 OWNER 멤버십 생성
- [ ] 기존 wallet 서비스를 재사용해 정산 USER 지갑 연결
- [ ] JWT 보호 Controller/DTO 추가
- [ ] missing/inactive/access-denied 예외와 오류 매핑
- [ ] cross-merchant 접근 IT 작성

## Acceptance

요청 body의 사용자/지갑 ID를 신뢰하지 않고 JWT subject와 DB 소유권만 사용한다.
