# Flows

## 결제 생성

```text
JWT 사용자
 -> 가맹점 활성 멤버십 확인(OWNER/MANAGER/CASHIER)
 -> 활성 정산 USER 지갑 조회
 -> merchant_id + order_id 중복 확인
 -> CREATED payment 저장
 -> 128-bit 이상 QR 난수 생성, 해시 저장
 -> /v1/payment-qr/{rawToken} 반환
```

동일 주문과 동일 필드는 기존 결제를 반환한다. 금액·토큰 등 불변 필드가 다르면
`409 DUPLICATE_REQUEST_MISMATCH`다.

## QR 조회

토큰을 해시하여 DB에서 찾고 token/payment 상태와 `expires_at`을 검증한다. 응답에는
가맹점명, 금액, 토큰, 설명, 상태, 만료시간만 포함한다.

## 결제 승인

```text
JWT 고객 -> customer profile/wallet 조회
 -> CREATED + 미만료 조건으로 PROCESSING 원자적 claim
 -> InternalTransferService.create(..., reference_id=payment_<paymentId>)
 -> PAID + transfer_id 저장
```

조건부 UPDATE로 한 요청만 claim한다. 기존 내부 이체 멱등성이 이중 차감의 두 번째
안전장치다. 트랜잭션 실패 시 payment claim과 원장 변경을 함께 rollback한다.

## 상태

```text
CREATED -> PROCESSING -> PAID
                    \-> FAILED
CREATED -> EXPIRED
CREATED -> CANCELLED
```
