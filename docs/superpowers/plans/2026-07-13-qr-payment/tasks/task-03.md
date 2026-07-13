# Task 3: JWT authentication boundary

설계: [auth](../../../specs/2026-07-13-qr-payment/auth.md)

## Files

- `build.gradle.kts`
- `userauth/*`, `userauth/dto/*`
- `auth/AuthSignatureFilter.java`
- `userauth/UserAuthIT.java`

## Steps

- [ ] 최소 Spring Security/JWT 의존성 추가
- [ ] password hash, login, access 검증, refresh 회전, logout 구현
- [ ] refresh token 원문 대신 해시 저장
- [ ] 사용자 소유 `/v1` 경로만 Ed25519 filter에서 제외
- [ ] 기존 wallet/transfer가 계속 Ed25519를 요구하는 회귀 IT 작성

## Acceptance

JWT와 운영자 Ed25519 인증 경계가 겹치거나 우회되지 않는다.
