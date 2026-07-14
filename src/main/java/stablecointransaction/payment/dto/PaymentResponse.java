package stablecointransaction.payment.dto;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.payment.Payment;

public record PaymentResponse(
    UUID payment_id,
    UUID merchant_id,
    String order_id,
    String token,
    BigInteger amount,
    String description,
    String status,
    OffsetDateTime expires_at,
    String qr_payload,
    OffsetDateTime created_at) {

  public static PaymentResponse from(Payment payment, String qrPayload) {
    return new PaymentResponse(payment.getPaymentId(), payment.getMerchantId(),
        payment.getOrderId(), payment.getToken(), payment.getAmount(),
        payment.getDescription(), payment.getStatus(), payment.getExpiresAt(),
        qrPayload, payment.getCreatedAt());
  }

  public static PaymentResponse withStatus(Payment payment, String status, String qrPayload) {
    return new PaymentResponse(payment.getPaymentId(), payment.getMerchantId(),
        payment.getOrderId(), payment.getToken(), payment.getAmount(),
        payment.getDescription(), status, payment.getExpiresAt(),
        qrPayload, payment.getCreatedAt());
  }
}
