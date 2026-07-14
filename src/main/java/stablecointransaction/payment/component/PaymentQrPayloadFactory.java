package stablecointransaction.payment.component;

import java.time.OffsetDateTime;
import stablecointransaction.payment.Payment;
import stablecointransaction.payment.PaymentStatuses;
import stablecointransaction.payment.qr.PaymentQrIssuer;
import stablecointransaction.userauth.UserAuthPaths;
import org.springframework.stereotype.Component;

@Component
public class PaymentQrPayloadFactory {
  private final PaymentQrIssuer qrIssuer;

  public PaymentQrPayloadFactory(PaymentQrIssuer qrIssuer) {
    this.qrIssuer = qrIssuer;
  }

  public String create(Payment payment, OffsetDateTime now) {
    if (!PaymentStatuses.CREATED.equals(payment.getStatus())
        || !payment.getExpiresAt().isAfter(now)) {
      return null;
    }
    PaymentQrIssuer.IssuedQr qr = qrIssuer.issueOrReuse(
        payment.getPaymentId(), payment.getExpiresAt(), now);
    return UserAuthPaths.PAYMENT_QR_PREFIX + "/" + qr.rawToken();
  }
}
