package stablecointransaction.payment.dto;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.UUID;
import stablecointransaction.merchant.Merchant;
import stablecointransaction.payment.Payment;

public record PaymentQrResponse(
    UUID payment_id,
    String merchant_name,
    String token,
    BigInteger amount,
    String description,
    String status,
    OffsetDateTime expires_at) {

  public static PaymentQrResponse from(Payment payment, Merchant merchant) {
    return new PaymentQrResponse(payment.getPaymentId(), merchant.getMerchantName(),
        payment.getToken(), payment.getAmount(), payment.getDescription(),
        payment.getStatus(), payment.getExpiresAt());
  }
}
