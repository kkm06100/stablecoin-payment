# Payment Outbox Plan

결제 승인과 외부 API 호출을 Outbox 기반으로 분리한다.

## 원칙

- DB 변경과 Outbox 저장은 하나의 트랜잭션으로 처리한다.
- 외부 API 호출은 Outbox Worker에서 처리한다.
- 외부 요청에는 idempotency key를 사용한다.
- 일시적 외부 오류만 재시도한다.
- `InternalApplicationException`은 재시도하지 않는다.
- 외부 서비스는 mock 기반 IT로 검증한다.

## 작업 순서

1. Payment Outbox 데이터 모델
2. 결제 승인 흐름 Outbox 전환
3. Outbox Worker 구현
4. Retry/Idempotency 적용
5. 상태 조회 및 프론트 polling
6. Merchant 프로비저닝 확장
7. Mock IT와 JaCoCo 검증

상세 내용은 각 `tasks/task-*.md`에 작성한다.

## README 작성 규칙

README는 인덱스와 핵심 원칙만 유지한다. README 확장은 최소화하며, 상세 설계·구현 방법·테스트 시나리오는 반드시 개별 task 문서에 작성한다.
