# 바운디드 컨텍스트 경계

현재 애플리케이션은 payment 컨텍스트가 QR, 결제 상태, merchant 지갑, customer 조회, 외부 송금 조정을 한 패키지에서 참조하면서 비대해질 위험이 있다.

## 현재 적용한 경계

- `payment`: 결제 aggregate와 결제 전용 repository
- `payment.service`: 결제 생성·확인·merchant 조회·customer 조회·QR 조회 애플리케이션 서비스
- `payment.outbox`: 결제 외부 작업의 이벤트 모델, repository, worker
- `payment.qr`: QR 토큰 발급·검증에 필요한 모델과 포트
- `merchant`: merchant aggregate와 membership/wallet 모델
- `merchant.service`: merchant 생성과 조회 애플리케이션 서비스
- `merchant.outbox`: merchant 지갑 프로비저닝 이벤트와 worker
- `external`: Solana/OpenBao/transaction server 호출 adapter와 원격 오류

## 비대화 방지 기준

1. 다른 컨텍스트의 entity를 직접 변경하지 않는다. 필요한 작업은 command/port 또는 outbox 이벤트로 전달한다.
2. Controller가 직접 참조하는 애플리케이션 서비스만 `service` 패키지에 둔다. repository 조합이나 외부 호출은 service의 책임이 아니다.
3. 외부 API DTO·HTTP 상태·서명 로직은 `external` 안에 가둔다. 도메인은 원격 API의 응답 형식을 알지 않는다.
4. Outbox는 원래 aggregate와 같은 컨텍스트의 `*.outbox` 하위 패키지에 둔다. 공통 Outbox 추상화로 서로 다른 도메인의 payload를 섞지 않는다.
5. 조회와 명령을 한 Service에 함께 넣지 않는다. merchant/customer 조회처럼 권한 규칙이 다른 경우에도 별도 서비스로 둔다.

## 다음 분리 후보

결제 상태 이력과 정산은 payment에서 성장할 가능성이 높으므로, 실제 업무 규칙과 저장소가 늘어나면 `payment-history`와 `settlement` 컨텍스트로 분리한다. 단순히 패키지만 늘리는 것은 분리가 아니며, 별도 aggregate·port·트랜잭션 경계를 확보할 수 있을 때 분리한다.

## 전체 준수 전환 플랜

현재 구조는 패키지 경계는 만들었지만, 일부 Service가 다른 컨텍스트의 Repository·Entity와 외부 API 타입을 직접 참조한다. 아래 순서로 실제 의존 방향까지 정리한다.

### 1단계: 기준선 고정 및 의존관계 목록화

- `payment`, `merchant`, `user`, `userauth`, `external`의 공개 진입점을 정의한다.
- Controller가 직접 참조하는 클래스는 각 컨텍스트의 `service` 패키지로 제한한다.
- 다른 컨텍스트의 Entity, Repository, 외부 Client를 직접 참조하는 목록을 정적 검색으로 고정한다.
- 이 단계에서는 동작을 변경하지 않고 패키지별 허용 의존관계 표를 만든다.

완료 조건:

- 위반 후보가 파일 단위로 목록화되어 있다.
- 이후 단계마다 기존 API 동작을 보존할 통합 테스트가 존재한다.

### 2단계: External Port와 Adapter 분리

`external` 내부도 다음처럼 나눈다.

- `external.port`: 애플리케이션이 사용하는 최소 인터페이스
- `external.adapter`: HTTP, OpenBao 서명, Solana/Transaction Server 구현
- `external.dto`: 외부 응답 전용 DTO
- `external.exception`: 원격 오류 변환

구체적인 변경:

- `StablecoinTransactionClient`를 `WalletProvisioner`, `TokenAccountRegistrar`, `AssetTransferGateway`, `WalletReader` 같은 최소 Port로 분리한다.
- `StablecoinTransactionRestClient`는 Adapter에서만 Port를 구현한다.
- `RemoteWallet`, `RemoteTransfer`를 내부 Service와 Response DTO에서 제거한다.
- 외부 응답은 Adapter 내부 DTO에서 내부 결과 객체로 변환한다.
- 외부 HTTP 상태 코드와 OpenBao/서명 예외는 Adapter에서 `ExternalServiceException` 계열로 변환한다.

완료 조건:

- `service`, `payment`, `merchant`, `user` 패키지에서 `StablecoinTransactionRestClient`와 외부 DTO를 참조하지 않는다.
- 외부 API 호출은 `external.adapter`에서만 발생한다.
- Mock 기반 IT가 기존처럼 외부 Port를 Mock 처리할 수 있다.

### 3단계: UserAuth 책임 분리

현재 `UserAuthService`의 회원가입·로그인·토큰·Customer 생성 책임을 분리한다.

- `SignupService`: User와 인증용 Customer 생성 command orchestration
- `LoginService`: 자격 증명 검증과 Access/Refresh Token 발급
- `RefreshTokenService`: Refresh Token 회전·폐기·가족 단위 revoke
- `LogoutService`: Refresh Token 폐기
- `CustomerProvisioningService`: Customer Profile과 Primary Wallet 생성
- `CustomerWalletProvisioner`: 외부 Wallet 생성과 Token Account 등록 Port 호출

회원가입은 UserAuth가 Customer의 Entity를 직접 생성하지 않도록 `CustomerProvisioningPort`를 사용한다. Customer 생성과 외부 지갑 프로비저닝이 비동기화될 경우 Customer Outbox를 사용하며, 인증 응답의 상태 의미를 명확히 한다.

완료 조건:

- 각 Service가 하나의 use case만 공개한다.
- 로그인·갱신·로그아웃·회원가입 테스트가 서로 독립적으로 유지된다.
- UserAuth Service가 외부 Client와 Customer Repository를 직접 조합하지 않는다.

### 4단계: Payment의 Merchant/User 직접 참조 제거

Payment가 다른 컨텍스트의 Entity와 Repository를 직접 사용하지 않도록 읽기 전용 Port를 둔다.

- `MerchantPaymentContextPort`: merchant membership과 settlement wallet 확인
- `CustomerPaymentContextPort`: customer 상태와 primary wallet 확인
- `MerchantInfoPort`: QR 응답에 필요한 merchant 표시 정보 조회

각 Port의 구현은 해당 컨텍스트 또는 adapter 계층에 둔다. Payment Service는 `Merchant`, `MerchantWallet`, `CustomerProfile`, `CustomerWallet` Entity를 받지 않고 payment에 필요한 불변 값 객체만 받는다.

구체적인 변경:

- `PaymentCreationContextResolver`를 Payment 전용 입력 값 조합기로 변경한다.
- `PaymentConfirmationValidator`에서 Customer Repository 직접 참조를 제거한다.
- `PaymentQrQueryService`에서 Merchant Repository 직접 참조를 제거한다.
- 다른 컨텍스트의 상태 변경은 command Port 또는 Outbox 이벤트로만 수행한다.

완료 조건:

- `payment` 하위 코드에서 `merchant.*Repository`, `user.*Repository`, 타 컨텍스트 Entity import가 없다.
- Payment IT는 실제 컨텍스트 Port 구현을 사용하고, Service 단위 테스트는 Port Mock만 사용한다.

### 5단계: Service와 Worker의 책임 최종 정리

- `service`: HTTP use case의 입력 검증, 권한 확인, application orchestration
- `component` 또는 `domain`: 하나의 도메인 규칙·상태 변경
- `outbox`: 이벤트 저장, claim, retry, 외부 작업 orchestration
- `external.adapter`: 실제 원격 호출
- `repository`: 영속성 쿼리만 담당

특히 Outbox Worker가 결제 Entity의 상태 변경과 외부 호출을 모두 직접 처리하지 않도록 `ClaimProcessor`, `ExternalCallProcessor`, `ResultProcessor`, `FailureProcessor`를 유지한다. 외부 호출은 Worker가 Port를 호출하고, 성공·실패 반영은 별도의 DB 트랜잭션으로 처리한다.

완료 조건:

- Service 간 Service 직접 참조가 없다.
- Controller가 Repository, Entity, External Port를 직접 참조하지 않는다.
- Worker 외부 호출 실패 시 DB 상태가 재시도 가능한 상태로 남는다.
- idempotency key와 stale lock 복구 테스트가 통과한다.

### 6단계: 검증과 회귀 방지

- 패키지 의존관계 ArchUnit 테스트 추가
- `payment`가 `merchant`·`user`의 Repository/Entity를 참조하지 않는 규칙 추가
- `external.adapter` 외부의 HTTP Client 사용 금지 규칙 추가
- Controller가 `service` 외의 애플리케이션 객체를 주입받지 않는 규칙 추가
- 회원가입, merchant 생성, 결제 생성·확인·조회, Outbox 재시도에 대한 Mock 기반 IT 유지
- 전체 `test`와 JaCoCo를 실행하고, 구조 규칙 위반을 빌드 실패로 처리

최종 완료 기준은 패키지 이름이 아니라 다음 의존 방향이다.

```text
Controller -> Service -> Port / Domain / Repository
                              |
                              +-> Outbox

Outbox Worker -> External Port -> External Adapter -> Remote API
```

어떤 컨텍스트도 다른 컨텍스트의 내부 Entity·Repository를 직접 변경하지 않으며, 외부 API의 DTO·HTTP 세부사항도 내부 계층으로 유출하지 않는 상태를 목표로 한다.
