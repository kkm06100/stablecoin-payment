# Milestones

1. Identity persistence와 고객 USER 지갑 소유권
2. Merchant persistence와 멤버십 권한
3. JWT 인증 및 기존 Ed25519 회귀 보호
4. 가맹점 결제 생성과 QR 조회
5. 고객 승인과 내부 이체
6. 동시성·멱등성 acceptance 및 조회/만료

완료 조건은 동시 승인에도 internal transfer와 고객 차감이 정확히 한 번만 발생하고,
ledger postings 합계가 0인 것이다.
