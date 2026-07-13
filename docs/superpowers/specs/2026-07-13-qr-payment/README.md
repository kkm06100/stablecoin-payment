# QR Payment Design

수탁형 QR 결제 기능의 설계 문서 모음이다.

- [Scope](./scope.md)
- [Architecture](./architecture.md)
- [Authentication](./auth.md)
- [Data model](./data-model.md)
- [Flows](./flows.md)
- [API](./api.md)
- [Milestones](./milestones.md)

핵심 목표는 가맹점이 5분 유효 QR을 생성하고, 인증된 고객이 기존
`InternalTransferService`를 통해 고객 USER 지갑에서 가맹점 USER 지갑으로 한 번만
결제하도록 하는 것이다.
