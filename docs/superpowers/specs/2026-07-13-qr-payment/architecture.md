# Architecture

## 패키지 경계

```text
stablecointransaction.user       사용자와 고객 지갑 소유권
stablecointransaction.userauth   JWT 인증과 refresh session
stablecointransaction.merchant   가맹점, 멤버십, 정산 지갑
stablecointransaction.payment    결제, QR, 승인
stablecointransaction.transfer   기존 원장 기반 자금 이동
```

기존 `auth`는 운영자 Ed25519 인증 전용으로 유지한다. 사용자 JWT를 섞지 않는다.

Entity는 기존 코드처럼 연관 객체 대신 UUID FK를 직접 보관한다. Service와 Component는
구체 클래스를 사용하고 Repository 또는 실제 포트 외에는 인터페이스를 만들지 않는다.

## 트랜잭션 경계

결제와 내부 이체는 동일 Spring Boot 프로세스와 PostgreSQL을 사용한다. 결제 승인에서는
HTTP로 `/v1/transfers`를 호출하지 않고 `InternalTransferService`를 직접 호출하여 결제
상태 전이와 원장 변경을 한 DB 트랜잭션으로 묶는다.
