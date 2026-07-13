# Task 10: Query, expiry, and documentation

## Steps

- [ ] 기존 cursor 스타일로 가맹점 결제 단건/목록 조회
- [ ] 오래된 CREATED를 EXPIRED로 바꾸는 scheduler
- [ ] 조회와 승인에서는 항상 expires_at을 직접 검증
- [ ] integration 문서와 smoke test 갱신
- [ ] unit, 관련 IT, 전체 preflight 순서로 검증

## Acceptance

운영 조회와 만료 처리가 완료되고 전체 테스트가 회귀 없이 통과한다.
