# Authentication

## 경로 소유권

```text
/v1/user-auth/**       로그인/refresh 공개 경로
/v1/merchants/**       JWT + 활성 가맹점 멤버십
/v1/payment-qr/**      조회 공개, confirm은 JWT
기존 /v1/**            운영자 Ed25519
```

현재 `AuthSignatureFilter`는 모든 `/v1/*`를 검사한다. 사용자 소유 경로만 명시적으로
제외하고 Spring Security가 JWT를 검증해야 한다. 경로 문자열은 `UserAuthPaths`에서
관리한다.

JWT subject는 `user_id`다. Controller는 `user_id`, 고객 지갑 ID, 가맹점 지갑 ID,
`created_by`를 신뢰 가능한 요청 필드로 받지 않는다.

Refresh token은 원문이 아닌 해시만 DB에 저장하며 재발급할 때 회전시킨다.
